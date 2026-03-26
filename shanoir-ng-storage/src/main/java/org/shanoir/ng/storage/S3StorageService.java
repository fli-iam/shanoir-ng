/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.storage;

import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(S3StorageService.class);

    @Autowired
    private final S3Template s3Template;

    @Autowired
    private final S3Client s3Client;

    @Value("${storage.s3.studies-bucket-name}")
    private String studiesBucket;

    @Value("${storage.s3.datasets-bucket-name}")
    private String datasetsBucket;

    @Value("${storage.s3.preclinial-bucket-name}")
    private String preclinicalBucket;

    @Autowired
    private final Environment environment;

    public S3StorageService(S3Template s3Template, S3Client s3Client, Environment environment) {
        this.s3Template = s3Template;
        this.s3Client = s3Client;
        this.environment = environment;
    }

    @PostConstruct
    void init() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            ensureBucketExists();
        }
    }

    private void ensureBucketExists() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(studiesBucket).build());
            LOG.info("Dev bucket '{}' created", studiesBucket);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(datasetsBucket).build());
            LOG.info("Dev bucket '{}' created", datasetsBucket);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(preclinicalBucket).build());
            LOG.info("Dev bucket '{}' created", preclinicalBucket);
        } catch (BucketAlreadyOwnedByYouException e) {
            LOG.info("S3 buckets already exist.");
        }
    }

    @Override
    public String storeExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        String directory = EXAMINATION + examinationId;
        String key = directory + "/" + fileName;
        try {
            s3Template.upload(datasetsBucket, key, inputStream,
                    ObjectMetadata.builder().contentType(contentType).build());
            LOG.info("Stored datasets file to S3: s3://{}/{}", datasetsBucket, key);
            return getPublicLocationDatasets(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    @Override
    public String storePreclinicalExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        String directory = EXAMINATION + examinationId;
        String key = directory + "/" + fileName;
        try {
            s3Template.upload(preclinicalBucket, key, inputStream,
                    ObjectMetadata.builder().contentType(contentType).build());
            LOG.info("Stored preclinical file to S3: s3://{}/{}", preclinicalBucket, key);
            return getPublicLocationDatasets(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    @Override
    public String storePathologyModelData(Long pathologyModelId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        String directory = PATHOLOGY_MODEL + pathologyModelId;
        String key = directory + "/" + fileName;
        try {
            s3Template.upload(preclinicalBucket, key, inputStream,
                    ObjectMetadata.builder().contentType(contentType).build());
            LOG.info("Stored pathology model file to S3: s3://{}/{}", preclinicalBucket, key);
            return getPublicLocationDatasets(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    @Override
    public String storeStudyFile(Long studyId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        String directory = STUDY + studyId;
        String key = directory + "/" + fileName;
        try {
            s3Template.upload(studiesBucket, key, inputStream,
                    ObjectMetadata.builder().contentType(contentType).build());
            LOG.info("Stored studies file to S3: s3://{}/{}", studiesBucket, key);
            return getPublicLocationStudies(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    @Override
    public Resource loadExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            return s3Template.download(datasetsBucket, EXAMINATION + examinationId + "/" + fileName);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public Resource loadPreclinicalExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            return s3Template.download(preclinicalBucket, EXAMINATION + examinationId + "/" + fileName);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public Resource loadDatasets(String directory, String fileName) throws StorageException {
        try {
            return s3Template.download(datasetsBucket, directory + "/" + fileName);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public Resource loadPreclinical(String directory, String fileName) throws StorageException {
        try {
            return s3Template.download(preclinicalBucket, directory + "/" + fileName);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public long getFileSizeExtraData(Long examinationId, String fileName) throws StorageException {
        String key = EXAMINATION + examinationId + "/" + fileName;
        try {
            HeadObjectResponse metadata = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(datasetsBucket)
                    .key(key)
                    .build());
            return metadata.contentLength();
        } catch (NoSuchKeyException e) {
            return 0L;
        } catch (Exception e) {
            throw new StorageException("Failed to get S3 object size for: " + key, e);
        }
    }

    @Override
    public String getPublicLocationDatasets(String directory, String fileName) throws StorageException {
        try {
            return s3Template.createSignedGetURL(datasetsBucket, directory + "/" + fileName,
                    Duration.ofHours(1)).toString();
        } catch (Exception e) {
            throw new StorageException("S3 pre-sign failed for: " + fileName, e);
        }
    }

    @Override
    public void deleteDatasets(String directory, String fileName) throws StorageException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(datasetsBucket)
                    .key(directory + "/" + fileName)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete dataset S3 object: " + fileName, e);
        }
    }

    @Override
    public void deletePreclinical(String directory, String fileName) throws StorageException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(preclinicalBucket)
                    .key(directory + "/" + fileName)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete preclinical S3 object: " + fileName, e);
        }
    }

    @Override
    public void deleteExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(datasetsBucket)
                    .key(EXAMINATION + examinationId + "/" + fileName)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete dataset S3 object: " + fileName, e);
        }
    }

    @Override
    public void deletePreclinicalExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(preclinicalBucket)
                    .key(EXAMINATION + examinationId + "/" + fileName)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete preclinical S3 object: " + fileName, e);
        }
    }

    @Override
    public void deleteDirectoryDatasets(String directory) throws StorageException {
        deleteDirectoryFromBucket(datasetsBucket, directory);
    }

    @Override
    public void deleteDirectoryPreclinical(String directory) throws StorageException {
        deleteDirectoryFromBucket(preclinicalBucket, directory);
    }

    @Override
    public void deleteDirectoryExtraData(Long examinationId) throws StorageException {
        deleteDirectoryFromBucket(datasetsBucket, EXAMINATION + examinationId);
    }

    @Override
    public void deleteDirectoryPreclinicalExtraData(Long examinationId) throws StorageException {
        deleteDirectoryFromBucket(preclinicalBucket, EXAMINATION + examinationId);
    }

    @Override
    public void moveDatasets(String directory, String sourceFileName, String targetFileName)
            throws StorageException {
        moveInBucket(datasetsBucket, directory, sourceFileName, targetFileName);
    }

    @Override
    public Resource loadPathologyModelData(Long pathologyModelId, String fileName) throws StorageException {
        try {
            return s3Template.download(preclinicalBucket, PATHOLOGY_MODEL + pathologyModelId + "/" + fileName);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public Resource loadStudyFile(Long studyId, String fileName) throws StorageException {
        String directory = STUDY + studyId;
        try {
            return s3Template.download(studiesBucket, directory + "/" + fileName);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public String getPublicLocationStudies(String directory, String fileName) throws StorageException {
        try {
            return s3Template.createSignedGetURL(studiesBucket, directory + "/" + fileName,
                    Duration.ofHours(1)).toString();
        } catch (Exception e) {
            throw new StorageException("S3 pre-sign failed for: " + fileName, e);
        }
    }

    @Override
    public void deleteStudyFile(Long studyId, String fileName) throws StorageException {
        String directory = STUDY + studyId;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(studiesBucket)
                    .key(directory + "/" + fileName)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete study S3 object: " + fileName, e);
        }
    }

    @Override
    public void deleteDirectoryStudyFile(Long studyId) throws StorageException {
        String directory = STUDY + studyId;
        deleteDirectoryFromBucket(studiesBucket, directory);
    }

    @Override
    public void moveStudyFile(Long studyId, String sourceFileName, String targetFileName)
            throws StorageException {
        String directory = STUDY + studyId;
        moveInBucket(studiesBucket, directory, sourceFileName, targetFileName);
    }

    private void moveInBucket(String bucket, String directory,
            String sourceFileName, String targetFileName) throws StorageException {
        String sourceKey = directory + "/" + sourceFileName;
        String targetKey = directory + "/" + targetFileName;
        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(targetKey)
                    .build());
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(sourceKey)
                    .build());
            LOG.info("Moved S3 object: {}/{} -> {}", bucket, sourceKey, targetKey);
        } catch (Exception e) {
            throw new StorageException("Failed to move S3 object " + sourceKey + " -> " + targetKey, e);
        }
    }

    private void deleteDirectoryFromBucket(String bucket, String directory) throws StorageException {
        try {
            String continuationToken = null;
            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                        .bucket(bucket)
                        .prefix(directory + "/");
                if (continuationToken != null) {
                    requestBuilder.continuationToken(continuationToken);
                }
                ListObjectsV2Response listing = s3Client.listObjectsV2(requestBuilder.build());
                if (!listing.contents().isEmpty()) {
                    List<ObjectIdentifier> keys = listing.contents().stream()
                            .map(o -> ObjectIdentifier.builder().key(o.key()).build())
                            .collect(Collectors.toList());
                    s3Client.deleteObjects(DeleteObjectsRequest.builder()
                            .bucket(bucket)
                            .delete(Delete.builder().objects(keys).build())
                            .build());
                    LOG.info("Deleted {} objects under s3://{}/{}", keys.size(), bucket, directory);
                }
                continuationToken = listing.isTruncated() ? listing.nextContinuationToken() : null;
            } while (continuationToken != null);
        } catch (Exception e) {
            throw new StorageException("Failed to delete S3 directory: " + directory, e);
        }
    }

}