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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Each Bruker upload is converted in its own folder of the shared /tmp volume,
 * {@code <uploadBrukerFolder>/bruker/convert/<user id>/<file name>/<index>}, with the uploaded archive, the Bruker data
 * and the converted DICOM files. It stays after the DICOM files are read, as ms-datasets stores the uploaded archive
 * at the end of the import if the user asks for it: this removes them every night, and the folders left empty.
 */
@Component
public class BrukerConversionCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(BrukerConversionCleaner.class);

    /** A conversion where nothing changed for this long is not used any more. */
    static final Duration MAX_AGE = Duration.ofHours(24);

    /** Depth of the conversion folders in the convert folder: user id, file name, index. */
    private static final int CONVERSION_DEPTH = 3;

    @Value("${storage.file-system.uploadBrukerFolder}")
    private String brukerFolder;

    @Scheduled(cron = "${preclinical.bruker.cleanup-cron:0 0 3 * * *}")
    public void cleanUp() {
        cleanUp(Instant.now().minus(MAX_AGE));
    }

    /**
     * @param limit the conversions and the empty folders not modified since this instant are deleted
     */
    void cleanUp(Instant limit) {
        Path convertFolder = Paths.get(brukerFolder, BrukerApiController.BRUKER_FOLDER, BrukerApiController.CONVERT_FOLDER)
                .toAbsolutePath().normalize();
        if (!Files.isDirectory(convertFolder)) {
            return;
        }
        int deleted = 0;
        for (Path conversion : list(convertFolder, CONVERSION_DEPTH)) {
            if (convertFolder.relativize(conversion).getNameCount() == CONVERSION_DEPTH && lastModified(conversion).isBefore(limit)) {
                // deleteQuietly does not follow the symbolic links
                FileUtils.deleteQuietly(conversion.toFile());
                deleted++;
            }
        }
        // the folders left empty, deepest first; a folder just created for an upload is recent, so it is kept
        List<Path> folders = list(convertFolder, CONVERSION_DEPTH - 1);
        folders.sort(Comparator.comparingInt(Path::getNameCount).reversed());
        for (Path folder : folders) {
            if (!folder.equals(convertFolder) && isEmptyDirectory(folder) && lastModified(folder).isBefore(limit)) {
                FileUtils.deleteQuietly(folder.toFile());
            }
        }
        if (deleted > 0) {
            LOG.info("{} Bruker conversion(s) older than {} deleted from {}", deleted, MAX_AGE, convertFolder);
        }
    }

    private static List<Path> list(Path folder, int maxDepth) {
        try (Stream<Path> paths = Files.walk(folder, maxDepth)) {
            return paths.collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Could not list the Bruker conversions of {}", folder, e);
            return List.of();
        }
    }

    /**
     * @return the last modification of the path or of anything inside it; now if it can't be read, so it is kept
     */
    private static Instant lastModified(Path path) {
        try (Stream<Path> paths = Files.walk(path)) {
            return paths.map(BrukerConversionCleaner::lastModifiedTime).max(Comparator.naturalOrder()).orElse(Instant.now());
        } catch (IOException | RuntimeException e) {
            return Instant.now();
        }
    }

    private static Instant lastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            return Instant.now();
        }
    }

    private static boolean isEmptyDirectory(Path folder) {
        if (!Files.isDirectory(folder)) {
            return false;
        }
        try (Stream<Path> entries = Files.list(folder)) {
            return entries.findAny().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

}
