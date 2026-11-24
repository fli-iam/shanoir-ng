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

package org.shanoir.ng.preclinical.bruker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The entries of an uploaded Bruker zip file must be extracted inside the folder of the upload ("zip slip").
 */
class BrukerUnzipTest {

    @TempDir
    private Path tempDir;

    @Test
    void entriesAreExtractedInsideTheFolder() throws IOException {
        Path zip = zip("bruker.zip", "2dseq");
        Path folder = Files.createDirectories(tempDir.resolve("upload"));

        BrukerApiController.unzipBrukerArchive(zip, folder);

        assertEquals("content", Files.readString(folder.resolve("2dseq")));
    }

    @Test
    void entryLeavingTheFolderIsRefused() throws IOException {
        Path zip = zip("evil.zip", "../evil.txt");
        Path folder = Files.createDirectories(tempDir.resolve("upload"));

        assertThrows(IOException.class, () -> BrukerApiController.unzipBrukerArchive(zip, folder));
        assertFalse(Files.exists(tempDir.resolve("evil.txt")));
    }

    private Path zip(String zipName, String entryName) throws IOException {
        Path zip = tempDir.resolve(zipName);
        try (OutputStream out = Files.newOutputStream(zip); ZipOutputStream zipOut = new ZipOutputStream(out)) {
            zipOut.putNextEntry(new ZipEntry(entryName));
            zipOut.write("content".getBytes());
            zipOut.closeEntry();
        }
        return zip;
    }

}
