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
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

/**
 * Parses BIDS {@code acq_time} values from {@code *_sessions.tsv} and {@code *_scans.tsv} files.
 */
public final class BidsTsvDateParser {

    static final String CSV_SEPARATOR = "\t";

    private static final Logger LOG = LoggerFactory.getLogger(BidsTsvDateParser.class);

    private BidsTsvDateParser() {
    }

    /**
     * Parses a BIDS {@code acq_time} value into a {@link LocalDate}.
     */
    public static Optional<LocalDate> parseAcqTime(String dateAsString) {
        if (dateAsString == null) {
            return Optional.empty();
        }
        String trimmed = dateAsString.trim();
        if (trimmed.isEmpty() || "n/a".equalsIgnoreCase(trimmed)) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return Optional.of(LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate());
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return Optional.of(OffsetDateTime.parse(trimmed, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate());
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return Optional.of(ZonedDateTime.parse(trimmed, DateTimeFormatter.ISO_ZONED_DATE_TIME).toLocalDate());
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return Optional.of(Instant.parse(trimmed).atZone(ZoneId.of("UTC")).toLocalDate());
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            DateTimeFormatter flexible = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'][ ]HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][XXX][X][Z]");
            return Optional.of(LocalDateTime.parse(trimmed, flexible).toLocalDate());
        } catch (DateTimeParseException | IllegalArgumentException e) {
            LOG.debug("Could not parse BIDS acq_time [{}]", trimmed);
            return Optional.empty();
        }
    }

    /**
     * Reads session dates from {@code sub-*_sessions.tsv} at subject level.
     * Keys are normalized session labels (without {@code ses-} prefix when present in TSV).
     */
    public static Map<String, LocalDate> readDatesFromSessionsFile(File subjectFolder) throws IOException {
        Map<String, LocalDate> examDates = new HashMap<>();
        File[] sessionFiles = subjectFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("_sessions.tsv");
            }
        });
        if (sessionFiles == null || sessionFiles.length != 1 || sessionFiles[0].length() == 0) {
            return examDates;
        }
        File sessionFile = sessionFiles[0];
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        try (MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(sessionFile)) {
            if (!it.hasNext()) {
                return examDates;
            }
            List<String> columns = parseHeaderColumns(it.next()[0]);
            int sessionIdIndex = indexOfColumn(columns, "session_id");
            int dateIndex = indexOfColumn(columns, "acq_time");
            if (dateIndex == -1) {
                LOG.warn("No acq_time column in sessions file [{}]", sessionFile.getName());
                return examDates;
            }
            while (it.hasNext()) {
                String[] row = splitRow(it.next()[0], columns.size());
                if (row.length <= dateIndex) {
                    continue;
                }
                String sessionId = sessionIdIndex >= 0 && row.length > sessionIdIndex ? row[sessionIdIndex].trim() : "";
                Optional<LocalDate> parsed = parseAcqTime(row[dateIndex]);
                if (parsed.isEmpty()) {
                    LOG.warn("Could not parse acq_time [{}] in sessions file [{}] for session [{}]",
                            row[dateIndex], sessionFile.getName(), sessionId);
                    continue;
                }
                putSessionDate(examDates, sessionId, parsed.get());
            }
        }
        return examDates;
    }

    /**
     * Reads the earliest valid {@code acq_time} from {@code *_scans.tsv} in the given folder.
     */
    public static Optional<LocalDate> readEarliestAcqTimeFromScansFolder(File parentFolder) throws IOException {
        File[] scansFiles = parentFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("_scans.tsv");
            }
        });
        if (scansFiles == null || scansFiles.length != 1 || scansFiles[0].length() == 0) {
            return Optional.empty();
        }
        File scanFile = scansFiles[0];
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        try (MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(scanFile)) {
            if (!it.hasNext()) {
                return Optional.empty();
            }
            List<String> columns = parseHeaderColumns(it.next()[0]);
            int dateIndex = indexOfColumn(columns, "acq_time");
            if (dateIndex == -1) {
                LOG.warn("No acq_time column in scans file [{}]", scanFile.getName());
                return Optional.empty();
            }
            List<LocalDate> dates = new ArrayList<>();
            while (it.hasNext()) {
                String[] row = splitRow(it.next()[0], columns.size());
                if (row.length <= dateIndex) {
                    continue;
                }
                parseAcqTime(row[dateIndex]).ifPresent(dates::add);
            }
            if (dates.isEmpty()) {
                LOG.warn("No parseable acq_time values in scans file [{}]", scanFile.getName());
                return Optional.empty();
            }
            return dates.stream().min(Comparator.naturalOrder());
        }
    }

    /**
     * Looks up a session date using folder label ({@code t0}) or BIDS id ({@code ses-t0}).
     */
    public static LocalDate lookupSessionDate(Map<String, LocalDate> sessionDates, String sessionLabel) {
        if (sessionDates == null || sessionLabel == null) {
            return null;
        }
        LocalDate date = sessionDates.get(sessionLabel);
        if (date != null) {
            return date;
        }
        return sessionDates.get("ses-" + sessionLabel);
    }

    static List<String> parseHeaderColumns(String headerLine) {
        return Arrays.stream(headerLine.split(CSV_SEPARATOR, -1))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    static int indexOfColumn(List<String> columns, String name) {
        for (int i = 0; i < columns.size(); i++) {
            if (name.equalsIgnoreCase(columns.get(i))) {
                return i;
            }
        }
        return -1;
    }

    static String[] splitRow(String rowLine, int expectedColumns) {
        String[] parts = rowLine.split(CSV_SEPARATOR, -1);
        if (expectedColumns > 0 && parts.length < expectedColumns) {
            String[] padded = new String[expectedColumns];
            System.arraycopy(parts, 0, padded, 0, parts.length);
            for (int i = parts.length; i < expectedColumns; i++) {
                padded[i] = "";
            }
            return padded;
        }
        return parts;
    }

    private static void putSessionDate(Map<String, LocalDate> examDates, String sessionIdFromTsv, LocalDate date) {
        if (sessionIdFromTsv == null || sessionIdFromTsv.isEmpty()) {
            return;
        }
        String trimmed = sessionIdFromTsv.trim();
        examDates.put(trimmed, date);
        if (trimmed.startsWith("ses-")) {
            examDates.put(trimmed.substring("ses-".length()), date);
        } else {
            examDates.put("ses-" + trimmed, date);
        }
    }
}
