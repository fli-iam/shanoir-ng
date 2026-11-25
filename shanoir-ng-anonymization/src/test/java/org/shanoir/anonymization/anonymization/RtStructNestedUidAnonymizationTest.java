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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * An import pseudonymizes the CT, the RTSTRUCT and the SEG of one examination in a
 * single batch. The references inside the RTSTRUCT and the SEG (referenced study,
 * series, frame of reference and source images, however deeply nested) must follow
 * the new UIDs of the pseudonymized images, otherwise the viewer cannot overlay the
 * contours on the CT any more. The data is synthetic and built here, so the test
 * runs everywhere.
 */
public class RtStructNestedUidAnonymizationTest {

    private static final String STUDY_UID = "2.25.100000000000000000000000000000000001";
    private static final String CT_SERIES_UID = "2.25.100000000000000000000000000000000002";
    private static final String FRAME_OF_REFERENCE_UID = "2.25.100000000000000000000000000000000003";
    private static final String RT_SERIES_UID = "2.25.100000000000000000000000000000000004";
    private static final String SEG_SERIES_UID = "2.25.100000000000000000000000000000000005";
    private static final String CT_SOP_PREFIX = "2.25.2000000000000000000000000000000000";

    /** Detached Study Management (retired): the SOP class an RTSTRUCT names for its referenced study. */
    private static final String DETACHED_STUDY_MANAGEMENT_SOP_CLASS = "1.2.840.10008.3.1.2.3.1";

    private static final int SLICES = 3;

    private Path workDir;

    @Before
    public void setUp() throws IOException {
        workDir = Files.createTempDirectory("shanoir-rt-anon-test");
    }

    @After
    public void tearDown() throws IOException {
        if (workDir != null && Files.exists(workDir)) {
            try (Stream<Path> walk = Files.walk(workDir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    @Test
    public void nestedReferencesFollowThePseudonymizedImages() throws Exception {
        // the derived objects come FIRST in the batch: the implementation must not depend on the order
        File rtStruct = write("RTSTRUCT_std.dcm", rtStruct(true));
        File rtStructWithoutStudy = write("RTSTRUCT_ohif.dcm", rtStruct(false));
        File seg = write("SEG.dcm", seg());
        ArrayList<File> batch = new ArrayList<>(List.of(rtStruct, rtStructWithoutStudy, seg));
        for (int i = 0; i < SLICES; i++) {
            batch.add(write("CT_" + i + ".dcm", ct(i)));
        }

        String profile = AnonymizationRulesSingleton.getInstance().getProfiles().keySet().iterator().next();
        new AnonymizationServiceImpl().anonymizeForShanoir(batch, profile, "TEST^SUBJECT", "TEST",
                "1.4.9.12.34.1.8527.999999999999");

        Attributes ctOut = read(new File(workDir.toFile(), "CT_0.dcm"));
        String newStudy = ctOut.getString(Tag.StudyInstanceUID);
        String newSeries = ctOut.getString(Tag.SeriesInstanceUID);
        String newFor = ctOut.getString(Tag.FrameOfReferenceUID);
        assertNotEquals(CT_SERIES_UID, newSeries);
        assertNotEquals(FRAME_OF_REFERENCE_UID, newFor);
        Set<String> newCtSops = new HashSet<>();
        for (int i = 0; i < SLICES; i++) {
            Attributes ct = read(new File(workDir.toFile(), "CT_" + i + ".dcm"));
            assertNotEquals(CT_SOP_PREFIX + i, ct.getString(Tag.SOPInstanceUID));
            assertEquals("all slices keep sharing one series", newSeries, ct.getString(Tag.SeriesInstanceUID));
            newCtSops.add(ct.getString(Tag.SOPInstanceUID));
        }
        assertEquals(SLICES, newCtSops.size());

        for (String name : List.of("RTSTRUCT_std.dcm", "RTSTRUCT_ohif.dcm")) {
            Attributes rt = read(new File(workDir.toFile(), name));
            assertEquals(name, newStudy, rt.getString(Tag.StudyInstanceUID));
            assertNotEquals(name, RT_SERIES_UID, rt.getString(Tag.SeriesInstanceUID));
            Attributes frameItem = rt.getSequence(Tag.ReferencedFrameOfReferenceSequence).get(0);
            assertEquals(name, newFor, frameItem.getString(Tag.FrameOfReferenceUID));
            Sequence rtSeries = name.contains("std")
                    ? frameItem.getSequence(Tag.RTReferencedStudySequence).get(0).getSequence(Tag.RTReferencedSeriesSequence)
                    : frameItem.getSequence(Tag.RTReferencedSeriesSequence);
            assertEquals(name, newSeries, rtSeries.get(0).getString(Tag.SeriesInstanceUID));
            if (name.contains("std")) {
                assertEquals(name, newStudy, frameItem.getSequence(Tag.RTReferencedStudySequence).get(0)
                        .getString(Tag.ReferencedSOPInstanceUID));
            }
            assertSopsAreAmong(newCtSops, rtSeries.get(0).getSequence(Tag.ContourImageSequence), SLICES);
            Attributes contour = rt.getSequence(Tag.ROIContourSequence).get(0).getSequence(Tag.ContourSequence).get(0);
            assertSopsAreAmong(newCtSops, contour.getSequence(Tag.ContourImageSequence), 1);
            assertEquals(name, newFor, rt.getSequence(Tag.StructureSetROISequence).get(0)
                    .getString(Tag.ReferencedFrameOfReferenceUID));
        }

        Attributes segOut = read(new File(workDir.toFile(), "SEG.dcm"));
        assertEquals(newStudy, segOut.getString(Tag.StudyInstanceUID));
        assertEquals(newFor, segOut.getString(Tag.FrameOfReferenceUID));
        Attributes referencedSeries = segOut.getSequence(Tag.ReferencedSeriesSequence).get(0);
        assertEquals(newSeries, referencedSeries.getString(Tag.SeriesInstanceUID));
        assertSopsAreAmong(newCtSops, referencedSeries.getSequence(Tag.ReferencedInstanceSequence), SLICES);
        Attributes sourceImage = segOut.getSequence(Tag.PerFrameFunctionalGroupsSequence).get(0)
                .getSequence(Tag.DerivationImageSequence).get(0).getSequence(Tag.SourceImageSequence).get(0);
        assertTrue(newCtSops.contains(sourceImage.getString(Tag.ReferencedSOPInstanceUID)));

        // nothing of the original identifiers may survive anywhere in the batch
        Set<String> original = Set.of(STUDY_UID, CT_SERIES_UID, FRAME_OF_REFERENCE_UID, RT_SERIES_UID, SEG_SERIES_UID,
                CT_SOP_PREFIX + 0, CT_SOP_PREFIX + 1, CT_SOP_PREFIX + 2);
        for (File f : batch) {
            Set<String> leaked = new HashSet<>();
            collectUids(read(f), original, leaked);
            assertTrue(f.getName() + " still carries original UIDs " + leaked, leaked.isEmpty());
        }
    }

    @Test
    public void referencesToImagesOutsideTheBatchAreLeftUntouched() throws Exception {
        // an RTSTRUCT sent alone cannot be re-linked: its references are not invented
        File rtStruct = write("RTSTRUCT_alone.dcm", rtStruct(true));
        ArrayList<File> batch = new ArrayList<>(List.of(rtStruct));
        String profile = AnonymizationRulesSingleton.getInstance().getProfiles().keySet().iterator().next();
        new AnonymizationServiceImpl().anonymizeForShanoir(batch, profile, "TEST^SUBJECT", "TEST",
                "1.4.9.12.34.1.8527.999999999999");
        Attributes rt = read(rtStruct);
        Sequence rtSeries = rt.getSequence(Tag.ReferencedFrameOfReferenceSequence).get(0)
                .getSequence(Tag.RTReferencedStudySequence).get(0).getSequence(Tag.RTReferencedSeriesSequence);
        assertNotNull(rtSeries.get(0).getString(Tag.SeriesInstanceUID));
        assertEquals(SLICES, rtSeries.get(0).getSequence(Tag.ContourImageSequence).size());
    }

    private static void assertSopsAreAmong(Set<String> expected, Sequence referencedImages, int count) {
        assertNotNull(referencedImages);
        assertEquals(count, referencedImages.size());
        for (Attributes item : referencedImages) {
            assertTrue("dangling SOP reference " + item.getString(Tag.ReferencedSOPInstanceUID),
                    expected.contains(item.getString(Tag.ReferencedSOPInstanceUID)));
        }
    }

    private static void collectUids(Attributes attributes, Set<String> original, Set<String> leaked) {
        for (int tag : attributes.tags()) {
            if (attributes.getVR(tag) == VR.SQ) {
                for (Attributes item : attributes.getSequence(tag)) {
                    collectUids(item, original, leaked);
                }
            } else if (attributes.getVR(tag) == VR.UI) {
                for (String value : attributes.getStrings(tag)) {
                    if (original.contains(value)) {
                        leaked.add(value);
                    }
                }
            }
        }
    }

    private static Attributes ct(int index) {
        Attributes a = common(UID.CTImageStorage, "CT", CT_SERIES_UID, CT_SOP_PREFIX + index);
        a.setString(Tag.FrameOfReferenceUID, VR.UI, FRAME_OF_REFERENCE_UID);
        a.setInt(Tag.InstanceNumber, VR.IS, index + 1);
        a.setInt(Tag.Rows, VR.US, 2);
        a.setInt(Tag.Columns, VR.US, 2);
        a.setInt(Tag.BitsAllocated, VR.US, 16);
        a.setInt(Tag.BitsStored, VR.US, 16);
        a.setInt(Tag.HighBit, VR.US, 15);
        a.setInt(Tag.PixelRepresentation, VR.US, 0);
        a.setInt(Tag.SamplesPerPixel, VR.US, 1);
        a.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        a.setBytes(Tag.PixelData, VR.OW, new byte[8]);
        return a;
    }

    private static Attributes rtStruct(boolean withReferencedStudy) {
        Attributes a = common(UID.RTStructureSetStorage, "RTSTRUCT", RT_SERIES_UID, "2.25.300000000000000000000000000000000001");
        a.setString(Tag.StructureSetLabel, VR.SH, "SYNTH");
        Attributes frameItem = new Attributes();
        frameItem.setString(Tag.FrameOfReferenceUID, VR.UI, FRAME_OF_REFERENCE_UID);
        Attributes seriesItem = new Attributes();
        seriesItem.setString(Tag.SeriesInstanceUID, VR.UI, CT_SERIES_UID);
        seriesItem.newSequence(Tag.ContourImageSequence, SLICES);
        for (int i = 0; i < SLICES; i++) {
            seriesItem.getSequence(Tag.ContourImageSequence).add(imageReference(i));
        }
        if (withReferencedStudy) {
            Attributes studyItem = new Attributes();
            studyItem.setString(Tag.ReferencedSOPClassUID, VR.UI, DETACHED_STUDY_MANAGEMENT_SOP_CLASS);
            studyItem.setString(Tag.ReferencedSOPInstanceUID, VR.UI, STUDY_UID);
            studyItem.newSequence(Tag.RTReferencedSeriesSequence, 1).add(seriesItem);
            frameItem.newSequence(Tag.RTReferencedStudySequence, 1).add(studyItem);
        } else {
            // what the OHIF viewer exports: the series hangs directly below the frame of reference
            frameItem.newSequence(Tag.RTReferencedSeriesSequence, 1).add(seriesItem);
        }
        a.newSequence(Tag.ReferencedFrameOfReferenceSequence, 1).add(frameItem);

        Attributes roi = new Attributes();
        roi.setInt(Tag.ROINumber, VR.IS, 1);
        roi.setString(Tag.ReferencedFrameOfReferenceUID, VR.UI, FRAME_OF_REFERENCE_UID);
        roi.setString(Tag.ROIName, VR.LO, "SYNTH_ROI");
        a.newSequence(Tag.StructureSetROISequence, 1).add(roi);

        Attributes roiContour = new Attributes();
        roiContour.setInt(Tag.ReferencedROINumber, VR.IS, 1);
        Sequence contours = roiContour.newSequence(Tag.ContourSequence, SLICES);
        for (int i = 0; i < SLICES; i++) {
            Attributes contour = new Attributes();
            contour.setString(Tag.ContourGeometricType, VR.CS, "CLOSED_PLANAR");
            contour.setInt(Tag.NumberOfContourPoints, VR.IS, 3);
            contour.setDouble(Tag.ContourData, VR.DS, 0, 0, i, 1, 0, i, 1, 1, i);
            contour.newSequence(Tag.ContourImageSequence, 1).add(imageReference(i));
            contours.add(contour);
        }
        a.newSequence(Tag.ROIContourSequence, 1).add(roiContour);
        return a;
    }

    private static Attributes seg() {
        Attributes a = common(UID.SegmentationStorage, "SEG", SEG_SERIES_UID, "2.25.300000000000000000000000000000000002");
        a.setString(Tag.FrameOfReferenceUID, VR.UI, FRAME_OF_REFERENCE_UID);
        Attributes referencedSeries = new Attributes();
        referencedSeries.setString(Tag.SeriesInstanceUID, VR.UI, CT_SERIES_UID);
        Sequence referencedInstances = referencedSeries.newSequence(Tag.ReferencedInstanceSequence, SLICES);
        for (int i = 0; i < SLICES; i++) {
            referencedInstances.add(imageReference(i));
        }
        a.newSequence(Tag.ReferencedSeriesSequence, 1).add(referencedSeries);
        Sequence perFrame = a.newSequence(Tag.PerFrameFunctionalGroupsSequence, SLICES);
        for (int i = 0; i < SLICES; i++) {
            Attributes frame = new Attributes();
            Attributes derivation = new Attributes();
            derivation.newSequence(Tag.SourceImageSequence, 1).add(imageReference(i));
            frame.newSequence(Tag.DerivationImageSequence, 1).add(derivation);
            perFrame.add(frame);
        }
        return a;
    }

    private static Attributes imageReference(int index) {
        Attributes item = new Attributes();
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.CTImageStorage);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, CT_SOP_PREFIX + index);
        return item;
    }

    private static Attributes common(String sopClass, String modality, String seriesUid, String sopUid) {
        Attributes a = new Attributes();
        a.setString(Tag.SOPClassUID, VR.UI, sopClass);
        a.setString(Tag.SOPInstanceUID, VR.UI, sopUid);
        a.setString(Tag.StudyInstanceUID, VR.UI, STUDY_UID);
        a.setString(Tag.SeriesInstanceUID, VR.UI, seriesUid);
        a.setString(Tag.Modality, VR.CS, modality);
        a.setString(Tag.PatientName, VR.PN, "SYNTH^PATIENT");
        a.setString(Tag.PatientID, VR.LO, "SYNTH-PATIENT-ID");
        a.setString(Tag.StudyDate, VR.DA, "20260101");
        a.setString(Tag.StudyTime, VR.TM, "101500");
        a.setString(Tag.AccessionNumber, VR.SH, "SYNTH-ACC");
        return a;
    }

    private File write(String name, Attributes dataset) throws IOException {
        File file = new File(workDir.toFile(), name);
        Attributes fmi = dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian);
        try (DicomOutputStream dos = new DicomOutputStream(file)) {
            dos.writeDataset(fmi, dataset);
        }
        return file;
    }

    private static Attributes read(File file) throws IOException {
        try (DicomInputStream din = new DicomInputStream(file)) {
            return din.readDataset();
        }
    }
}
