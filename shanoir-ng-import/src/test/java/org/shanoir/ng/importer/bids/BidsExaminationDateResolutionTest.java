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

package org.shanoir.ng.importer.bids;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BidsExaminationDateResolutionTest {

    @Test
    void resolvePrefersSessionsTsv(@TempDir Path tempDir) throws IOException {
        Path sessionDir = Files.createDirectory(tempDir.resolve("ses-t0"));
        Map<String, LocalDate> sessions = Map.of("t0", LocalDate.of(2024, 6, 1));
        FileTime folderTime = FileTime.from(Instant.parse("2026-05-29T12:00:00Z"));

        BidsExaminationDateResolution resolution = BidsExaminationDateResolution.resolve(
                sessionDir.toFile(), "t0", sessions, folderTime, ZoneId.systemDefault());

        assertFalse(resolution.isFallback());
        assertEquals(LocalDate.of(2024, 6, 1), resolution.getDate());
        assertTrue(resolution.getSourceDescription().contains("sessions.tsv"));
    }

    @Test
    void resolveUsesScansTsvWhenNoSessionDate(@TempDir Path tempDir) throws IOException {
        Path sessionDir = tempDir.resolve("ses-t0");
        Files.createDirectories(sessionDir);
        Files.writeString(sessionDir.resolve("sub-117_ses-t0_scans.tsv"),
                "filename\tacq_time\nscan.nii.gz\t2024-12-13T17:48:00\n");
        FileTime folderTime = FileTime.from(Instant.parse("2026-05-29T12:00:00Z"));

        BidsExaminationDateResolution resolution = BidsExaminationDateResolution.resolve(
                sessionDir.toFile(), "t0", Collections.emptyMap(), folderTime, ZoneId.systemDefault());

        assertFalse(resolution.isFallback());
        assertEquals(LocalDate.of(2024, 12, 13), resolution.getDate());
        assertTrue(resolution.getSourceDescription().contains("scans.tsv"));
    }

    @Test
    void resolveFallbackToFolderTime(@TempDir Path tempDir) throws IOException {
        Path sessionDir = Files.createDirectory(tempDir.resolve("ses-t0"));
        FileTime folderTime = FileTime.from(Instant.parse("2026-05-29T12:00:00Z"));

        BidsExaminationDateResolution resolution = BidsExaminationDateResolution.resolve(
                sessionDir.toFile(), "t0", Collections.emptyMap(), folderTime, ZoneId.of("UTC"));

        assertTrue(resolution.isFallback());
        assertEquals(LocalDate.of(2026, 5, 29), resolution.getDate());
        assertTrue(resolution.formatEventMessage().startsWith("Warning:"));
    }
}
