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

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves examination date for BIDS import with explicit source for logging/events.
 */
public final class BidsExaminationDateResolution {

    private final LocalDate date;
    private final String sourceDescription;
    private final boolean fallback;

    public BidsExaminationDateResolution(LocalDate date, String sourceDescription, boolean fallback) {
        this.date = date;
        this.sourceDescription = sourceDescription;
        this.fallback = fallback;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public boolean isFallback() {
        return fallback;
    }

    public String formatEventMessage() {
        if (fallback) {
            return "Warning: " + sourceDescription + " (date: " + date + ")";
        }
        return "Examination date from " + sourceDescription + " (" + date + ")";
    }

    /**
     * Priority: sessions.tsv (matching session) → scans.tsv (earliest acq_time) → folder creation time.
     */
    public static BidsExaminationDateResolution resolve(File sessionFolder, String sessionLabel,
            Map<String, LocalDate> sessionDatesFromSubjectTsv, FileTime folderCreationTime, ZoneId fallbackZone)
            throws IOException {
        LocalDate fromSessions = BidsTsvDateParser.lookupSessionDate(sessionDatesFromSubjectTsv, sessionLabel);
        if (fromSessions != null) {
            return new BidsExaminationDateResolution(fromSessions,
                    "sub-*_sessions.tsv (acq_time for session " + sessionLabel + ")", false);
        }
        Optional<LocalDate> fromScans = BidsTsvDateParser.readEarliestAcqTimeFromScansFolder(sessionFolder);
        if (fromScans.isPresent()) {
            return new BidsExaminationDateResolution(fromScans.get(),
                    "session *_scans.tsv (earliest acq_time)", false);
        }
        LocalDate fallbackDate = LocalDate.ofInstant(folderCreationTime.toInstant(), fallbackZone);
        return new BidsExaminationDateResolution(fallbackDate,
                "folder creation time — no parseable acq_time in sessions.tsv or scans.tsv", true);
    }

    /**
     * For subject folder without {@code ses-*} subfolders: scans.tsv then folder date (UTC).
     */
    public static BidsExaminationDateResolution resolveWithoutSessionFolder(File subjectOrSessionFolder,
            FileTime folderCreationTime) throws IOException {
        Optional<LocalDate> fromScans = BidsTsvDateParser.readEarliestAcqTimeFromScansFolder(subjectOrSessionFolder);
        if (fromScans.isPresent()) {
            return new BidsExaminationDateResolution(fromScans.get(),
                    "*_scans.tsv (earliest acq_time)", false);
        }
        LocalDate fallbackDate = LocalDate.ofInstant(folderCreationTime.toInstant(), ZoneOffset.UTC);
        return new BidsExaminationDateResolution(fallbackDate,
                "folder creation time — no parseable acq_time in scans.tsv", true);
    }
}
