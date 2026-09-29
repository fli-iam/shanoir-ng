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
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BidsTsvDateParserTest {

    @Test
    void parseAcqTimeIsoDateTime() {
        assertEquals(LocalDate.of(2024, 12, 13),
                BidsTsvDateParser.parseAcqTime("2024-12-13T17:48:00").get());
    }

    @Test
    void parseAcqTimeWithFractionalSeconds() {
        assertEquals(LocalDate.of(2024, 12, 13),
                BidsTsvDateParser.parseAcqTime("2024-12-13T17:48:00.123456").get());
    }

    @Test
    void parseAcqTimeWithZuluOffset() {
        assertEquals(LocalDate.of(2024, 12, 13),
                BidsTsvDateParser.parseAcqTime("2024-12-13T17:48:00Z").get());
    }

    @Test
    void parseAcqTimeDateOnly() {
        assertEquals(LocalDate.of(2024, 12, 13),
                BidsTsvDateParser.parseAcqTime("2024-12-13").get());
    }

    @Test
    void parseAcqTimeInvalidReturnsEmpty() {
        assertFalse(BidsTsvDateParser.parseAcqTime("not-a-date").isPresent());
        assertFalse(BidsTsvDateParser.parseAcqTime("").isPresent());
        assertFalse(BidsTsvDateParser.parseAcqTime(null).isPresent());
    }

    @Test
    void readEarliestAcqTimeFromScansFolder(@TempDir Path tempDir) throws IOException {
        Path sessionDir = tempDir.resolve("ses-t0");
        Files.createDirectories(sessionDir);
        String tsv = "filename\tacq_time\n"
                + "anat/sub-117_ses-t0_run-02_T1w.nii.gz\t2024-12-14T10:00:00\n"
                + "anat/sub-117_ses-t0_run-01_T1w.nii.gz\t2024-12-13T17:48:00\n";
        Files.writeString(sessionDir.resolve("sub-117_ses-t0_scans.tsv"), tsv);

        Optional<LocalDate> date = BidsTsvDateParser.readEarliestAcqTimeFromScansFolder(sessionDir.toFile());
        assertTrue(date.isPresent());
        assertEquals(LocalDate.of(2024, 12, 13), date.get());
    }

    @Test
    void readDatesFromSessionsFileNormalizesSessionId(@TempDir Path tempDir) throws IOException {
        Path subjectDir = tempDir.resolve("sub-117");
        Files.createDirectories(subjectDir);
        String tsv = "session_id\tacq_time\n"
                + "ses-t0\t2024-12-13T08:00:00\n";
        Files.writeString(subjectDir.resolve("sub-117_sessions.tsv"), tsv);

        Map<String, LocalDate> dates = BidsTsvDateParser.readDatesFromSessionsFile(subjectDir.toFile());
        assertEquals(LocalDate.of(2024, 12, 13), dates.get("t0"));
        assertEquals(LocalDate.of(2024, 12, 13), dates.get("ses-t0"));
        assertEquals(LocalDate.of(2024, 12, 13), BidsTsvDateParser.lookupSessionDate(dates, "t0"));
    }

    @Test
    void readEarliestAcqTimeMissingAcqTimeColumn(@TempDir Path tempDir) throws IOException {
        Path sessionDir = tempDir.resolve("ses-t0");
        Files.createDirectories(sessionDir);
        Files.writeString(sessionDir.resolve("sub-117_ses-t0_scans.tsv"), "filename\tsize\nscan.nii.gz\t1\n");

        assertFalse(BidsTsvDateParser.readEarliestAcqTimeFromScansFolder(sessionDir.toFile()).isPresent());
    }

    @Test
    void parseHeaderColumnsTrimsNames() {
        assertEquals(2, BidsTsvDateParser.parseHeaderColumns(" filename \t acq_time ").size());
        assertEquals("acq_time", BidsTsvDateParser.parseHeaderColumns(" filename \t acq_time ").get(1));
    }
}
