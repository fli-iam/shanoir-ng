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

import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(S3StorageService.class);

    private static final long MULTIPART_THRESHOLD = 100 * 1024 * 1024L; // 100 MB

    private static final long PART_SIZE = 8 * 1024 * 1024L; // 8 MB

    private static final String S3 = "s3:///"; // empty host name

    @Autowired
    private final S3Template s3Template;

    @Autowired
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${storage.s3.studies.bucket:UNUSED}")
    private String studiesBucket;

    @Value("${storage.s3.datasets.bucket:UNUSED}")
    private String datasetsBucket;

    @Value("${storage.s3.preclinical.bucket:UNUSED}")
    private String preclinicalBucket;

    @Value("${storage.s3.studies.prefix:UNUSED}")
    private String studiesPrefix;

    @Value("${storage.s3.datasets.prefix:UNUSED}")
    private String datasetsPrefix;

    @Value("${storage.s3.preclinical.prefix:UNUSED}")
    private String preclinicalPrefix;

    @Autowired
    private final Environment environment;

    public S3StorageService(S3Template s3Template, S3Client s3Client, Environment environment) {
        this.s3Template = s3Template;
        this.s3Client = s3Client;
        this.environment = environment;
    }

    @PostConstruct
    void init() throws StorageException {
        if (!studiesPrefix.equals("UNUSED")) {
            ensureValidPrefix(studiesPrefix, "studies");
            ensureBucketExists(studiesBucket, "studies");
        }
        if (!datasetsPrefix.equals("UNUSED")) {
            ensureValidPrefix(datasetsPrefix, "datasets");
            ensureBucketExists(datasetsBucket, "datasets");
        }
        if (!preclinicalPrefix.equals("UNUSED")) {
            ensureValidPrefix(preclinicalPrefix, "preclinical");
            ensureBucketExists(preclinicalBucket, "preclinical");
        }
    }

    private void ensureValidPrefix(String prefix, String label) throws StorageException {
        if ((prefix != "") && (!prefix.endsWith(SLASH))) {
            LOG.error("Bucket '{}' is configured with an invalid prefix '{}' (prefix must be empty or end with the '{}' delimiter)", label, prefix, SLASH);
            throw new StorageException("Invalid S3 prefix", null);
        }
    }

    private void ensureBucketExists(String bucketName, String label) {
        if ("UNUSED".equals(bucketName)) {
            LOG.info("Bucket '{}' is not configured (UNUSED), skipping creation.", label);
            return;
        }
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            LOG.info("Bucket '{}' ({}) created successfully.", label, bucketName);
        } catch (BucketAlreadyOwnedByYouException e) {
            LOG.info("Bucket '{}' ({}) already exists, skipping.", label, bucketName);
        } catch (Exception e) {
            LOG.error("Failed to create bucket '{}' ({}): {}", label, bucketName, e.getMessage(), e);
        }
    }

    @Override
    public String storeStudyData(Long studyId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (studiesBucket.equals(UNUSED)) {
            throw new StorageException("Missing studies bucket configuration.", null);
        }
        String directory = studiesPrefix + STUDY + studyId;
        String key = directory + SLASH + fileName;
        try {
            if (size > MULTIPART_THRESHOLD) {
                uploadMultipart(studiesBucket, key, inputStream, contentType, size);
            } else {
                s3Template.upload(studiesBucket, key, inputStream,
                        ObjectMetadata.builder().contentType(contentType).build());
            }
            LOG.info("Stored studies file to S3: s3://{}/{}", studiesBucket, key);
            return getPublicLocationStudies(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    @Override
    public String storeExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (datasetsBucket.equals(UNUSED)) {
            throw new StorageException("Missing datasets bucket configuration.", null);
        }
        String directory = datasetsPrefix + EXAMINATION + examinationId;
        String key = directory + SLASH + fileName;
        try {
            if (size > MULTIPART_THRESHOLD) {
                uploadMultipart(datasetsBucket, key, inputStream, contentType, size);
            } else {
                s3Template.upload(datasetsBucket, key, inputStream,
                        ObjectMetadata.builder().contentType(contentType).build());
            }
            LOG.info("Stored datasets extra-data file to S3: s3://{}/{}", datasetsBucket, key);
            return getPublicLocationDatasets(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    @Override
    public String storeBIDSData(Long studyId, String subjectName, Long examinationId, String fileName,
            String dataTypeBIDS, InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (datasetsBucket.equals(UNUSED)) {
            throw new StorageException("Missing datasets bucket configuration.", null);
        }
        String directory = datasetsPrefix
                + STUDY + studyId
                + SLASH + SUBJECT + subjectName
                + SLASH + SESSION + examinationId
                + SLASH + dataTypeBIDS;
        String key = directory + SLASH + fileName;
        try {
            if (size > MULTIPART_THRESHOLD) {
                uploadMultipart(datasetsBucket, key, inputStream, contentType, size);
            } else {
                s3Template.upload(datasetsBucket, key, inputStream,
                        ObjectMetadata.builder().contentType(contentType).build());
            }
            LOG.info("Stored BIDS data file to S3: s3://{}/{}", datasetsBucket, key);
            return getPublicLocationDatasets(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    private void uploadMultipart(String bucket, String key, InputStream inputStream,
            String contentType, long size) throws StorageException {
        CreateMultipartUploadResponse initResponse = s3Client.createMultipartUpload(r -> r
                .bucket(bucket)
                .key(key)
                .contentType(contentType));
        String uploadId = initResponse.uploadId();
        List<CompletedPart> completedParts = new ArrayList<>();
        int partNumber = 1;
        try {
            byte[] buffer = new byte[(int) PART_SIZE];
            int bytesRead;
            while ((bytesRead = readFully(inputStream, buffer)) > 0) {
                final int partNum = partNumber;
                final int length = bytesRead;
                UploadPartResponse partResponse = s3Client.uploadPart(
                        r -> r.bucket(bucket)
                                .key(key)
                                .uploadId(uploadId)
                                .partNumber(partNum)
                                .contentLength((long) length),
                        RequestBody.fromByteBuffer(ByteBuffer.wrap(buffer, 0, length)));
                completedParts.add(CompletedPart.builder()
                        .partNumber(partNum)
                        .eTag(partResponse.eTag())
                        .build());
                LOG.debug("Uploaded part {}/{} for key: {}", partNum,
                        (int) Math.ceil((double) size / PART_SIZE), key);
                partNumber++;
            }
            s3Client.completeMultipartUpload(r -> r
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(m -> m.parts(completedParts)));
        } catch (Exception e) {
            // Always abort to avoid orphaned multipart uploads (= unexpected S3 charges)
            LOG.error("Multipart upload failed for key: {}. Aborting upload {}.", key, uploadId, e);
            s3Client.abortMultipartUpload(r -> r
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId));
            throw new StorageException("Multipart S3 upload failed for: " + key, e);
        }
    }

    /**
     * Reads as many bytes as possible into the buffer (handles partial reads from
     * stream).
     * Returns total bytes read, or -1 at end-of-stream.
     */
    private int readFully(InputStream is, byte[] buffer) throws IOException {
        int totalRead = 0;
        int bytesRead;
        while (totalRead < buffer.length
                && (bytesRead = is.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
            totalRead += bytesRead;
        }
        return totalRead == 0 ? -1 : totalRead;
    }

    @Override
    public String storePreclinicalExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (preclinicalBucket.equals(UNUSED)) {
            throw new StorageException("Missing preclinical bucket configuration.", null);
        }
        String directory = preclinicalPrefix + EXAMINATION + examinationId;
        String key = directory + SLASH + fileName;
        try {
            if (size > MULTIPART_THRESHOLD) {
                uploadMultipart(preclinicalBucket, key, inputStream, contentType, size);
            } else {
                s3Template.upload(preclinicalBucket, key, inputStream,
                    ObjectMetadata.builder().contentType(contentType).build());
            }
            LOG.info("Stored preclinical extra-data to S3: s3://{}/{}", preclinicalBucket, key);
            return getPublicLocationPreclinical(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    @Override
    public String storePathologyModelData(Long pathologyModelId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (preclinicalBucket.equals(UNUSED)) {
            throw new StorageException("Missing preclinical bucket configuration.", null);
        }
        String directory = preclinicalPrefix + PATHOLOGY_MODEL + pathologyModelId;
        String key = directory + SLASH + fileName;
        try {
            if (size > MULTIPART_THRESHOLD) {
                uploadMultipart(preclinicalBucket, key, inputStream, contentType, size);
            } else {
                s3Template.upload(preclinicalBucket, key, inputStream,
                        ObjectMetadata.builder().contentType(contentType).build());
            }
            LOG.info("Stored pathology model file to S3: s3://{}/{}", preclinicalBucket, key);
            return getPublicLocationPreclinical(directory, fileName);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException("S3 upload failed for: " + fileName, e);
        }
    }

    @Override
    public Resource loadExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            String key = datasetsPrefix + EXAMINATION + examinationId + SLASH + fileName;
            return s3Template.download(datasetsBucket, key);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public Resource loadPreclinicalExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            String key = preclinicalPrefix + EXAMINATION + examinationId + SLASH + fileName;
            return s3Template.download(preclinicalBucket, key);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public long getFileSizeExtraData(Long examinationId, String fileName) throws StorageException {
        String key = datasetsPrefix + EXAMINATION + examinationId + SLASH + fileName;
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

    /**
     * The getPublicLocationXXX methods can easily be extended with
     * s3Template.createSignedGetURL calls for storing fully functional, signed URLs.
     */
    @Override
    public String getPublicLocationDatasets(String directory, String fileName) throws StorageException {
        try {
            return S3 + datasetsBucket + SLASH + directory + SLASH + fileName;
        } catch (Exception e) {
            throw new StorageException("S3 URL build failed for: " + fileName, e);
        }
    }

    @Override
    public String getPublicLocationPreclinical(String directory, String fileName) throws StorageException {
        try {
            return S3 + preclinicalBucket + SLASH + directory + SLASH + fileName;
        } catch (Exception e) {
            throw new StorageException("S3 URL build failed for: " + fileName, e);
        }
    }

    @Override
    public void deleteExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            String key = datasetsPrefix
                            + EXAMINATION + examinationId + SLASH + fileName;
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(datasetsBucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete dataset S3 object: " + fileName, e);
        }
    }

    @Override
    public void deletePreclinicalExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            String key = preclinicalPrefix
                            + EXAMINATION + examinationId + SLASH + fileName;
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(preclinicalBucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete preclinical S3 object: " + fileName, e);
        }
    }

    @Override
    public void deletePathologyModelData(Long pathologyModelId, String fileName) throws StorageException {
        try {
            String key = preclinicalPrefix
                    + PATHOLOGY_MODEL + pathologyModelId + SLASH + fileName;
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(preclinicalBucket)
                    .key(key)
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
        deleteDirectoryFromBucket(datasetsBucket, datasetsPrefix + EXAMINATION + examinationId);
    }

    @Override
    public void deleteDirectoryPreclinicalExtraData(Long examinationId) throws StorageException {
        deleteDirectoryFromBucket(preclinicalBucket, preclinicalPrefix + EXAMINATION + examinationId);
    }

    @Override
    public void moveDatasets(String directory, String sourceFileName, String targetFileName)
            throws StorageException {
        moveInBucket(datasetsBucket, directory, sourceFileName, targetFileName);
    }

    @Override
    public Resource loadPathologyModelData(Long pathologyModelId, String fileName) throws StorageException {
        try {
            String key = preclinicalPrefix + PATHOLOGY_MODEL + pathologyModelId + SLASH + fileName;
            return s3Template.download(preclinicalBucket, key);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public Resource loadStudyData(Long studyId, String fileName) throws StorageException {
        String key = studiesPrefix + STUDY + studyId + SLASH + fileName;
        try {
            return s3Template.download(studiesBucket, key);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + fileName, e);
        }
    }

    @Override
    public String getPublicLocationStudies(String directory, String fileName) throws StorageException {
        try {
            return S3 + studiesBucket + SLASH + directory + SLASH + fileName;
        } catch (Exception e) {
            throw new StorageException("S3 URL build failed for: " + fileName, e);
        }
    }

    @Override
    public void deleteStudyData(Long studyId, String fileName) throws StorageException {
        String key = studiesPrefix + STUDY + studyId + SLASH + fileName;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(studiesBucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete study S3 object: " + fileName, e);
        }
    }

    @Override
    public void deleteDirectoryStudyData(Long studyId) throws StorageException {
        String directory = studiesPrefix + STUDY + studyId;
        deleteDirectoryFromBucket(studiesBucket, directory);
    }

    @Override
    public void moveStudyData(Long studyId, String sourceFileName, String targetFileName)
            throws StorageException {
        String directory = studiesPrefix + STUDY + studyId;
        moveInBucket(studiesBucket, directory, sourceFileName, targetFileName);
    }

    private void moveInBucket(String bucket, String directory,
            String sourceFileName, String targetFileName) throws StorageException {
        String sourceKey = directory + SLASH + sourceFileName;
        String targetKey = directory + SLASH + targetFileName;
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
                        .prefix(directory + SLASH);
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
