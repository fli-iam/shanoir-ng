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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The file names given to the storage come from the clients: they must stay inside their folder.
 * The base folders are inside a temporary folder that also holds a "secret.txt" file, outside of them.
 */
class FileSystemStorageServiceTest {

    private static final byte[] CONTENT = "content".getBytes(StandardCharsets.UTF_8);

    @TempDir
    private Path root;

    private Path secret;

    private FileSystemStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new FileSystemStorageService();
        ReflectionTestUtils.setField(service, "baseDirStudies", root.resolve("studies").toString());
        ReflectionTestUtils.setField(service, "baseDirDatasets", root.resolve("datasets").toString());
        ReflectionTestUtils.setField(service, "baseDirPreclinical", root.resolve("preclinical").toString());
        Files.createDirectories(root.resolve("studies").resolve("study-1"));
        Files.createDirectories(root.resolve("datasets").resolve("examination-1"));
        secret = Files.write(root.resolve("secret.txt"), CONTENT);
    }

    @Test
    void storesLoadsAndDeletesAFileInItsFolder() throws Exception {
        String location = service.storeExtraData(1L, "data file.txt", new ByteArrayInputStream(CONTENT), "text/plain", CONTENT.length);

        Path stored = root.resolve("datasets").resolve("examination-1").resolve("data file.txt");
        assertEquals("file://" + stored.toAbsolutePath().normalize(), location);
        assertTrue(service.existsExtraData(1L, "data%20file.txt"));
        assertEquals(stored.toFile(), service.loadExtraData(1L, "data%20file.txt").getFile());
        assertEquals(CONTENT.length, service.getFileSizeExtraData(1L, "data file.txt"));
        service.deleteExtraData(1L, "data file.txt");
        assertFalse(Files.exists(stored));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../examination-2/data.txt", "../../secret.txt", "/tmp/data.txt", "..\\data.txt", "..", ".", ""})
    void refusesToStoreANameLeavingItsFolder(String fileName) {
        assertThrows(StorageException.class, () -> service.storeExtraData(
                1L, fileName, new ByteArrayInputStream(CONTENT), "text/plain", CONTENT.length));
        assertThrows(StorageException.class, () -> service.storeStudyData(
                1L, fileName, new ByteArrayInputStream(CONTENT), "text/plain", CONTENT.length));
        assertThrows(StorageException.class, () -> service.storePreclinicalExtraData(
                1L, fileName, new ByteArrayInputStream(CONTENT), "text/plain", CONTENT.length));
        assertFalse(Files.exists(root.resolve("datasets").resolve("examination-2")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../../secret.txt", "..%2F..%2Fsecret.txt", "%2E%2E%2F%2E%2E%2Fsecret.txt"})
    void refusesToReadAFileOutsideOfItsFolder(String fileName) {
        assertThrows(StorageException.class, () -> service.loadStudyData(1L, fileName));
        assertThrows(StorageException.class, () -> service.loadExtraData(1L, fileName));
        assertThrows(StorageException.class, () -> service.existsStudyData(1L, fileName));
        assertThrows(StorageException.class, () -> service.existsExtraData(1L, fileName));
    }

    @Test
    void refusesToGiveTheSizeOfAFileOutsideOfItsFolder() {
        // the file size methods do not decode the name
        assertThrows(StorageException.class, () -> service.getFileSizeExtraData(1L, "../../secret.txt"));
        assertThrows(StorageException.class, () -> service.getFileSizeAcquisitionExtraData(1L, "../../secret.txt"));
    }

    @Test
    void refusesToReadADatasetFileOutsideOfTheDatasetsFolder() {
        assertThrows(StorageException.class, () -> service.loadDatasetsData("../secret.txt"));
        assertThrows(StorageException.class, () -> service.loadDatasetsData("study-1/../../secret.txt"));
    }

    @Test
    void refusesToDeleteOrMoveAFileOutsideOfItsFolder() throws IOException {
        Files.write(root.resolve("studies").resolve("study-1").resolve("dua.pdf"), CONTENT);

        assertThrows(StorageException.class, () -> service.deleteExtraData(1L, "../../secret.txt"));
        assertThrows(StorageException.class, () -> service.deleteStudyData(1L, "../../secret.txt"));
        assertThrows(StorageException.class, () -> service.moveStudyData(1L, "dua.pdf", "../../moved.pdf"));
        assertThrows(StorageException.class, () -> service.moveStudyData(1L, "../../secret.txt", "stolen.txt"));
        assertTrue(Files.exists(secret));
        assertTrue(Files.exists(root.resolve("studies").resolve("study-1").resolve("dua.pdf")));
        assertFalse(Files.exists(root.resolve("moved.pdf")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "", "examination-1/../.."})
    void refusesToDeleteADirectoryOutsideOfItsFolder(String directory) {
        assertThrows(StorageException.class, () -> service.deleteDirectoryDatasets(directory));
        assertTrue(Files.exists(secret));
        assertTrue(Files.exists(root.resolve("datasets").resolve("examination-1")));
    }

}
