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

package org.shanoir.ng.shared.storage;

import java.io.InputStream;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(S3StorageService.class);

    @Autowired
    private S3Template s3Template;

    @Autowired
    private S3Client s3Client;

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    public S3StorageService(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    @Override
    public void store(String directory, String filename,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        try {
            s3Template.upload(
                    bucketName,
                    directory + "/" + filename,
                    inputStream,
                    ObjectMetadata.builder().contentType(contentType).build()
            );
        } catch (Exception e) {
            throw new StorageException("S3 upload failed for: " + filename, e);
        }
    }

    @Override
    public Resource load(String directory, String filename) throws StorageException {
        try {
            return s3Template.download(bucketName, directory + "/" + filename);
        } catch (Exception e) {
            throw new StorageException("S3 download failed for: " + filename, e);
        }
    }

    @Override
    public String getPublicLocation(String directory, String filename) throws StorageException {
        try {
            return s3Template.createSignedGetURL(
                    bucketName,
                    directory + "/" + filename,
                    Duration.ofHours(1)).toString();
        } catch (Exception e) {
            throw new StorageException("S3 pre-sign failed for: " + filename, e);
        }
    }

    @Override
    public void delete(String directory, String filename) throws StorageException {
        try {
            s3Template.deleteObject(bucketName, directory + "/" + filename);
        } catch (Exception e) {
            throw new StorageException("S3 delete failed for: " + filename, e);
        }
    }

    @Override
    public void move(String directory, String sourceFilename, String targetFilename)
            throws StorageException {
        String sourceKey = directory + "/" + sourceFilename;
        String targetKey = directory + "/" + targetFilename;
        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(targetKey)
                    .build());
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceKey)
                    .build());
            LOG.info("Moved S3 object: {} -> {}", sourceKey, targetKey);
        } catch (S3Exception e) {
            throw new StorageException("Failed to move S3 object " + sourceKey + " -> " + targetKey, e);
        }
    }

}

