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

package org.shanoir.ng.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The path of the Bruker archive stored at the end of an import comes from the client: only an archive in the Bruker
 * conversion folder of the user is accepted.
 */
class ImporterServiceBrukerArchiveTest {

    @TempDir
    private Path root;

    private Path convert;

    private Path archive;

    private ImporterService service;

    @BeforeEach
    void setUp() throws IOException {
        convert = Files.createDirectories(root.resolve("bruker/convert"));
        archive = Files.write(Files.createDirectories(convert.resolve("12/2dseq/0")).resolve("2dseq.zip"), new byte[] {1});
        Files.write(Files.createDirectories(convert.resolve("13/exam/0")).resolve("exam.zip"), new byte[] {1});
        Files.write(root.resolve("secret.zip"), new byte[] {1});
        service = new ImporterService();
        ReflectionTestUtils.setField(service, "brukerConvertDir", convert.toString());
    }

    @Test
    void acceptsAnArchiveOfTheUser() {
        assertEquals(archive.toFile(), service.brukerArchive(archive.toString(), 12L));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "13/exam/0/exam.zip", // another user's archive
        "12/../13/exam/0/exam.zip",
        "12/../../secret.zip",
        "12/2dseq/0", // a folder
        "12",
        "12/2dseq/0/missing.zip",
    })
    void refusesAnyOtherPath(String relativePath) {
        assertNull(service.brukerArchive(convert + "/" + relativePath, 12L));
    }

    @Test
    void refusesAFileOutsideOfTheConvertFolder() {
        assertNull(service.brukerArchive(root.resolve("secret.zip").toString(), 12L));
        assertNull(service.brukerArchive("/etc/hostname", 12L));
    }

    @Test
    void refusesASymbolicLink() throws IOException {
        Path link = Files.createSymbolicLink(convert.resolve("12/2dseq/0/link.zip"), root.resolve("secret.zip"));

        assertNull(service.brukerArchive(link.toString(), 12L));
    }

    @Test
    void refusesWithoutUserOrWithAnInvalidPath() {
        assertNull(service.brukerArchive(archive.toString(), null));
        assertNull(service.brukerArchive(archive + "\0", 12L));
    }

}
