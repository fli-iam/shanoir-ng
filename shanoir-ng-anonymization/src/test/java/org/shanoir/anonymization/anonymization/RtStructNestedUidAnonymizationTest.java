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

package org.shanoir.anonymization.anonymization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Test;

/**
 * Verifies that RTSTRUCT nested references are remapped when CT slices of the
 * same import batch are pseudonymized first.
 */
public class RtStructNestedUidAnonymizationTest {

    private static final Path SESSION = Path.of(
            "C:", "Users", "james", "Documents", "data", "Target", "sub-740813", "ses-20190701100121");

    private static final Path RT = SESSION.resolve(
            "1_RTP_1_0_Natif/RTSTRUCT_1_2_246_352_222_400_2545060576_2072_1575533859_22626.dcm");

    private static final Path CT_DIR = SESSION.resolve("2_RTP_1_0_Natif");

    @Test
    public void nestedRtReferencesMatchPseudonymizedCtInSameBatch() throws Exception {
        assumeTrue("Colleague sample data not on disk", RT.toFile().isFile() && Files.isDirectory(CT_DIR));

        Path workDir = Files.createTempDirectory("shanoir-rt-anon-test");
        try {
            ArrayList<File> batch = new ArrayList<>();
            try (Stream<Path> ctFiles = Files.list(CT_DIR).filter(p -> p.toString().endsWith(".dcm"))) {
                for (Path p : ctFiles.sorted(Comparator.comparing(Path::toString)).toList()) {
                    copyTo(workDir, p, batch);
                }
            }
            copyTo(workDir, RT, batch);

            String profile = AnonymizationRulesSingleton.getInstance().getProfiles().keySet().iterator().next();
            AnonymizationServiceImpl anonymizer = new AnonymizationServiceImpl();
            anonymizer.anonymizeForShanoir(batch, profile, "TEST^SUBJECT", "TEST", "1.4.9.12.34.1.8527.999999999999");

            File rtOut = batch.stream().filter(f -> f.getName().startsWith("RTSTRUCT")).findFirst().orElseThrow();
            File ctOut = batch.stream().filter(f -> f.getName().startsWith("CT_")).findFirst().orElseThrow();

            Attributes rt = readWithoutPixels(rtOut);
            Attributes ct = readWithoutPixels(ctOut);
            var rs = rt.getSequence(org.dcm4che3.data.Tag.ReferencedFrameOfReferenceSequence).get(0)
                    .getSequence(org.dcm4che3.data.Tag.RTReferencedStudySequence).get(0)
                    .getSequence(org.dcm4che3.data.Tag.RTReferencedSeriesSequence).get(0);

            assertEquals(ct.getString(org.dcm4che3.data.Tag.StudyInstanceUID), rt.getString(org.dcm4che3.data.Tag.StudyInstanceUID));
            assertEquals(ct.getString(org.dcm4che3.data.Tag.SeriesInstanceUID), rs.getString(org.dcm4che3.data.Tag.SeriesInstanceUID));
            assertEquals(ct.getString(org.dcm4che3.data.Tag.FrameOfReferenceUID), rt.getString(org.dcm4che3.data.Tag.FrameOfReferenceUID));
            assertEquals(ct.getString(org.dcm4che3.data.Tag.FrameOfReferenceUID),
                    rt.getSequence(org.dcm4che3.data.Tag.ReferencedFrameOfReferenceSequence).get(0)
                            .getString(org.dcm4che3.data.Tag.FrameOfReferenceUID));

            String contourSop = rs.getSequence(org.dcm4che3.data.Tag.ContourImageSequence).get(0)
                    .getString(org.dcm4che3.data.Tag.ReferencedSOPInstanceUID);
            assertEquals(ct.getString(org.dcm4che3.data.Tag.SOPInstanceUID), contourSop);

            long matches = 0;
            var contourSops = rs.getSequence(org.dcm4che3.data.Tag.ContourImageSequence);
            java.util.Set<String> ctSops = new java.util.HashSet<>();
            for (File ctFile : batch) {
                if (ctFile.getName().startsWith("CT_")) {
                    ctSops.add(readWithoutPixels(ctFile).getString(org.dcm4che3.data.Tag.SOPInstanceUID));
                }
            }
            for (Attributes contour : contourSops) {
                if (ctSops.contains(contour.getString(org.dcm4che3.data.Tag.ReferencedSOPInstanceUID))) {
                    matches++;
                }
            }
            assertEquals(contourSops.size(), matches);
            assertTrue(matches > 0);
        } finally {
            deleteRecursively(workDir);
        }
    }

    private static void copyTo(Path workDir, Path source, ArrayList<File> batch) throws java.io.IOException {
        Path target = workDir.resolve(source.getFileName().toString());
        Files.copy(source, target);
        batch.add(target.toFile());
    }

    private static Attributes readWithoutPixels(File file) throws java.io.IOException {
        try (DicomInputStream din = new DicomInputStream(file)) {
            din.readFileMetaInformation();
            return din.readDataset(org.dcm4che3.data.Tag.PixelData);
        }
    }

    private static void deleteRecursively(Path root) throws java.io.IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
