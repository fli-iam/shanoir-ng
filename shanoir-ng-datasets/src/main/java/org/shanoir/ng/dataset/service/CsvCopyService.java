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

package org.shanoir.ng.dataset.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.shanoir.ng.dataset.model.CopyReport;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CsvCopyService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvCopyService.class);

    public static final String TSV_FILE_PREFIX = "ShanoirCopyReport";
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    public void writeReportTsvFile(List<CopyReport> cvsReports, Long eventId) {
        String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
        Path filePath = Paths.get(userDir + File.separator + TSV_FILE_PREFIX + "_" + eventId + ".tsv");
        try {
            // columns : source dataset id, target dataset id, subject name
            StringBuilder sb = new StringBuilder();
            sb.append("source_dataset_id\t target_dataset_id\t subject_new_name\n");
            // for each copied dataset, we add a line in the tsv file
            for (CopyReport report : cvsReports) {
                sb
                        .append(report.getSourceDatasetId())
                        .append("\t").append(report.getTargetDatasetId())
                        .append("\t").append(report.getSubjectNewName())
                        .append("\n");
            }
            // Write the TSV content to a file
            Files.write(filePath, sb.toString().getBytes());
        } catch (IOException e) {
            LOG.error("Error while writing copy report tsv file for event {}.", eventId, e);
        }
    }

    // cron every 5 minutes to delete old tsv files (older than 6 hours)
    @Scheduled(cron = "0 0/5 * * * ?")
    public void deleteOldTsvFiles() {
        LOG.info("CRON - delete old tsv files");

        String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        long expirationTime = System.currentTimeMillis() - 6 * 60 * 60 * 1000;

        try (Stream<Path> paths = Files.walk(Paths.get(tmpDir), 2)) {
            paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith(TSV_FILE_PREFIX))
                .filter(path -> isOlderThan(path, expirationTime))
                    .forEach(this::deleteFileSafely);
        } catch (IOException e) {
            LOG.error("Error while listing user directories", e);
        }
    }

    private boolean isOlderThan(Path path, long expirationTime) {
        try {
            return Files.getLastModifiedTime(path).toMillis() < expirationTime;
        } catch (IOException e) {
            LOG.error("Error while checking age for {}", path, e);
            return false;
        }
    }

    private void deleteFileSafely(Path path) {
        try {
            Files.delete(path);
            LOG.info("Deleted old TSV file {}", path);
        } catch (IOException e) {
            LOG.error("Error while deleting {}", path, e);
        }
    }
}
