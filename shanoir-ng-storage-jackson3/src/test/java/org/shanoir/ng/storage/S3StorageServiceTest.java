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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * The file names given to the storage come from the clients: they must not lead to another S3 key prefix.
 */
class S3StorageServiceTest {

    private S3Template s3Template;

    private S3Client s3Client;

    private S3StorageService service;

    @BeforeEach
    void setUp() {
        s3Template = mock(S3Template.class);
        s3Client = mock(S3Client.class);
        service = new S3StorageService(s3Template, s3Client, mock(Environment.class));
        ReflectionTestUtils.setField(service, "studiesBucket", "studies-bucket");
        ReflectionTestUtils.setField(service, "datasetsBucket", "datasets-bucket");
        ReflectionTestUtils.setField(service, "preclinicalBucket", "preclinical-bucket");
        ReflectionTestUtils.setField(service, "studiesPrefix", "studies/");
        ReflectionTestUtils.setField(service, "datasetsPrefix", "datasets/");
        ReflectionTestUtils.setField(service, "preclinicalPrefix", "preclinical/");
    }

    private static InputStream content() {
        return new ByteArrayInputStream(new byte[] {1});
    }

    @Test
    void storesAFileUnderItsPrefix() throws StorageException {
        service.storeStudyData(1L, "dua.pdf", content(), "application/pdf", 1);
        verify(s3Template).upload(eq("studies-bucket"), eq("studies/study-1/dua.pdf"), any(InputStream.class), any(ObjectMetadata.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../study-2/dua.pdf", "../../dua.pdf", "folder/dua.pdf", "..\\dua.pdf", "..", ".", ""})
    void refusesANameLeadingToAnotherPrefix(String fileName) {
        assertThrows(StorageException.class, () -> service.storeStudyData(1L, fileName, content(), "application/pdf", 1));
        assertThrows(StorageException.class, () -> service.storeExtraData(1L, fileName, content(), "text/plain", 1));
        assertThrows(StorageException.class, () -> service.storePathologyModelData(1L, fileName, content(), "text/plain", 1));
        assertThrows(StorageException.class, () -> service.loadStudyData(1L, fileName));
        assertThrows(StorageException.class, () -> service.loadPreclinicalExtraData(1L, fileName));
        assertThrows(StorageException.class, () -> service.existsExtraData(1L, fileName));
        assertThrows(StorageException.class, () -> service.getFileSizeExtraData(1L, fileName));
        assertThrows(StorageException.class, () -> service.deleteStudyData(1L, fileName));
        assertThrows(StorageException.class, () -> service.deleteAcquisitionExtraData(1L, fileName));
        assertThrows(StorageException.class, () -> service.moveStudyData(1L, "dua.pdf", fileName));
        verifyNoInteractions(s3Template, s3Client);
    }

    @Test
    void refusesAPathWithADotDotSegment() {
        assertThrows(StorageException.class, () -> service.loadDatasetsData("/datasets-bucket/datasets/../studies/study-1/dua.pdf"));
        assertThrows(StorageException.class, () -> service.deleteDirectoryDatasets("datasets/study-1/.."));
        assertThrows(StorageException.class, () -> service.moveDatasets("datasets/..", "a.nii", "b.nii"));
        verifyNoInteractions(s3Template, s3Client);
    }

}
