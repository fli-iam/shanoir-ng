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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Bruker conversions are in {@code <uploadBrukerFolder>/bruker/convert/<user id>/<file name>/<index>}: the ones
 * where nothing changed for 24 hours are deleted every night, then the folders left empty.
 */
class BrukerConversionCleanerTest {

    private static final Instant NOW = Instant.now();

    private static final Instant TWO_DAYS_AGO = NOW.minus(Duration.ofDays(2));

    @TempDir
    private Path root;

    private Path convert;

    private BrukerConversionCleaner cleaner;

    @BeforeEach
    void setUp() throws IOException {
        convert = Files.createDirectories(root.resolve("bruker/convert"));
        cleaner = new BrukerConversionCleaner();
        ReflectionTestUtils.setField(cleaner, "brukerFolder", root.toString() + "/");
    }

    /** A conversion with an archive, Bruker data and converted files, all modified at the given instant. */
    private Path conversion(String relativePath, Instant modified) throws IOException {
        Path conversion = Files.createDirectories(convert.resolve(relativePath));
        Files.write(conversion.resolve("upload.zip"), new byte[] {1});
        Files.write(Files.createDirectories(conversion.resolve("result")).resolve("1.dcm"), new byte[] {1});
        Files.write(conversion.resolve("result123.0.converted.zip"), new byte[] {1});
        setModified(conversion, modified);
        return conversion;
    }

    private static void setModified(Path path, Instant modified) throws IOException {
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path p : paths.toList()) {
                Files.setLastModifiedTime(p, FileTime.from(modified));
            }
        }
    }

    private void clean() {
        cleaner.cleanUp(NOW.minus(BrukerConversionCleaner.MAX_AGE));
    }

    @Test
    void deletesTheConversionsOlderThan24Hours() throws IOException {
        Path old = conversion("12/2dseq/0", TWO_DAYS_AGO);
        Path recent = conversion("12/2dseq/1", NOW.minus(Duration.ofHours(1)));
        Path otherUserOld = conversion("13/exam/0", TWO_DAYS_AGO);

        clean();

        assertFalse(Files.exists(old));
        assertFalse(Files.exists(otherUserOld));
        assertTrue(Files.exists(recent.resolve("result/1.dcm")));
    }

    @Test
    void keepsAnOldConversionWithARecentFile() throws IOException {
        // a conversion still being written: its folder is old, but not its last file
        Path conversion = conversion("12/2dseq/0", TWO_DAYS_AGO);
        Files.setLastModifiedTime(conversion.resolve("result/1.dcm"), FileTime.from(NOW));

        clean();

        assertTrue(Files.exists(conversion.resolve("upload.zip")));
    }

    @Test
    void deletesTheOldEmptyFoldersButNotTheRecentOnes() throws IOException {
        Path oldEmpty = Files.createDirectories(convert.resolve("12/2dseq"));
        Files.setLastModifiedTime(oldEmpty, FileTime.from(TWO_DAYS_AGO));
        Files.setLastModifiedTime(convert.resolve("12"), FileTime.from(TWO_DAYS_AGO));
        // a folder just created for an upload, before its conversion folder
        Path justCreated = Files.createDirectories(convert.resolve("13/exam"));

        clean();

        assertFalse(Files.exists(oldEmpty));
        assertTrue(Files.exists(justCreated));
        assertTrue(Files.exists(convert));
    }

    @Test
    void deletesTheConversionsOfTheFormerLayout() throws IOException {
        // before the user folders: <file name>/<index>/..., so the files of a conversion are at the depth of a conversion
        Path former = conversion("2dseq/0", TWO_DAYS_AGO);
        setModified(convert.resolve("2dseq"), TWO_DAYS_AGO);

        clean();

        assertFalse(Files.exists(former.resolve("upload.zip")));
        assertFalse(Files.exists(former.resolve("result")));
        // the folders left empty were just modified: they go the next night
        cleaner.cleanUp(NOW.plus(Duration.ofDays(1)));

        assertFalse(Files.exists(convert.resolve("2dseq")));
        assertTrue(Files.exists(convert));
    }

    @Test
    void doesNothingWithoutConvertFolder() throws IOException {
        Files.delete(convert);

        clean();

        assertEquals(List.of(root.resolve("bruker")), Files.list(root).toList());
    }

    @Test
    void runsEveryNight() throws NoSuchMethodException {
        Scheduled scheduled = BrukerConversionCleaner.class.getMethod("cleanUp").getAnnotation(Scheduled.class);
        assertNotNull(scheduled);
        assertEquals("${preclinical.bruker.cleanup-cron:0 0 3 * * *}", scheduled.cron());
        assertNotNull(ShanoirPreclinicalApplication.class.getAnnotation(EnableScheduling.class));
    }

}
