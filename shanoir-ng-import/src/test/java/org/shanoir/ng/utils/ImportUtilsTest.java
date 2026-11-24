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

package org.shanoir.ng.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.mock.web.MockMultipartFile;

/**
 * The entry names of an uploaded zip file come from the client: the files must be extracted inside
 * the import folder ("zip slip").
 */
class ImportUtilsTest {

    private static final byte[] CONTENT = "content".getBytes(StandardCharsets.UTF_8);

    @TempDir
    private Path root;

    private Path importDir;

    @BeforeEach
    void setUp() throws IOException {
        importDir = Files.createDirectories(root.resolve("import").resolve("12"));
    }

    private Path zip(String... entryNames) throws IOException {
        Path zip = importDir.resolve("123.upload");
        try (OutputStream out = Files.newOutputStream(zip); ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (String name : entryNames) {
                zipOut.putNextEntry(new ZipEntry(name));
                if (!name.endsWith("/")) {
                    zipOut.write(CONTENT);
                }
                zipOut.closeEntry();
            }
        }
        return zip;
    }

    @Test
    void unzipExtractsTheFilesAndFoldersInsideTheFolder() throws IOException {
        // a file entry before the entry of its folder, and a "./" entry
        Path zip = zip("subject/exam/1.dcm", "./", "subject/", "subject/exam/", "DICOMDIR", "subject/./exam/../2.dcm");
        Path dest = importDir.resolve("123");

        ImportUtils.unzip(zip.toString(), dest.toString());

        assertArrayEquals(CONTENT, Files.readAllBytes(dest.resolve("subject/exam/1.dcm")));
        assertArrayEquals(CONTENT, Files.readAllBytes(dest.resolve("DICOMDIR")));
        assertArrayEquals(CONTENT, Files.readAllBytes(dest.resolve("subject/2.dcm")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../evil.txt", "subject/../../evil.txt", "../../../evil.txt", "/tmp/evil.txt"})
    void unzipRefusesAnEntryLeavingTheFolder(String entryName) throws IOException {
        Path zip = zip("subject/1.dcm", entryName);
        Path dest = importDir.resolve("123");

        assertThrows(ZipException.class, () -> ImportUtils.unzip(zip.toString(), dest.toString()));

        assertFalse(Files.exists(importDir.resolve("evil.txt")));
        assertFalse(Files.exists(root.resolve("import").resolve("evil.txt")));
        assertFalse(Files.exists(root.resolve("evil.txt")));
    }

    @Test
    void anUploadedZipLeavingItsFolderIsRefusedAndNothingIsKept() throws IOException {
        Path zip = zip("subject/1.dcm", "../../evil.txt");
        MockMultipartFile upload = new MockMultipartFile("file", "exam.zip", "application/zip", CONTENT);

        RestServiceException e = assertThrows(RestServiceException.class,
                () -> ImportUtils.saveTempFileCreateFolderAndUnzip(zip.toFile(), upload, true));

        assertEquals(422, e.getErrorModel().getCode());
        assertFalse(Files.exists(root.resolve("import").resolve("evil.txt")));
        assertFalse(Files.exists(importDir.resolve("123")));
        assertFalse(Files.exists(zip));
    }

    @Test
    void anUploadedZipIsExtractedInAFolderOfItsName() throws Exception {
        Path zip = zip("subject/1.dcm");
        MockMultipartFile upload = new MockMultipartFile("file", "exam.zip", "application/zip", CONTENT);

        Path folder = ImportUtils.saveTempFileCreateFolderAndUnzip(zip.toFile(), upload, true).toPath();

        assertEquals(importDir.resolve("123"), folder);
        assertTrue(Files.exists(folder.resolve("subject/1.dcm")));
        assertFalse(Files.exists(zip));
    }

}
