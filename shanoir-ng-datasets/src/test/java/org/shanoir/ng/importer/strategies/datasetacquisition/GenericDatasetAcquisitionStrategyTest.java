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

package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.time.LocalDateTime;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.importer.dto.Serie;

/**
 * Tests the acquisition start time handling of GenericDatasetAcquisitionStrategy.
 *
 * The generic strategy is the default one, so it receives the non-image IODs
 * accepted by DicomUtils.checkSerieIsIgnored: RTSTRUCT, RTDOSE and RTPLAN. Those
 * have no AcquisitionDate/AcquisitionTime, which used to fail the whole import
 * with a NullPointerException.
 */
public class GenericDatasetAcquisitionStrategyTest {

    private static final String USER_NAME = "test";

    private final GenericDatasetAcquisitionStrategy strategy = new GenericDatasetAcquisitionStrategy();

    private Serie generateSerie(String modality) {
        Serie serie = new Serie();
        serie.setModality(modality);
        serie.setSeriesInstanceUID("1.2.3.4.5");
        serie.setSeriesDescription("test serie");
        return serie;
    }

    private LocalDateTime acquisitionStartTimeOf(Attributes attributes, String modality) throws Exception {
        DatasetAcquisition acquisition = strategy.generateFlatDatasetAcquisitionForSerie(
                USER_NAME, generateSerie(modality), 0, attributes);
        return acquisition.getAcquisitionStartTime();
    }

    /**
     * An RTSTRUCT has neither AcquisitionDate/AcquisitionTime nor ContentDate/ContentTime:
     * the import must not fail and must fall back to StructureSetDate/StructureSetTime.
     */
    @Test
    public void testRtStructFallsBackToStructureSetDateAndTime() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.StructureSetDate, VR.DA, "20180424");
        attributes.setString(Tag.StructureSetTime, VR.TM, "122100");

        Assertions.assertEquals(LocalDateTime.of(2018, 4, 24, 12, 21, 0),
                acquisitionStartTimeOf(attributes, "RTSTRUCT"));
    }

    /**
     * Non-conformant RTSTRUCT files exist with no usable date/time at all. The acquisition
     * start time is optional, so the import must still succeed with a null value.
     */
    @Test
    public void testRtStructWithoutAnyDateDoesNotThrow() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.SeriesDescription, VR.LO, "structure set");

        Assertions.assertNull(acquisitionStartTimeOf(attributes, "RTSTRUCT"));
    }

    /**
     * A DICOM date/time tag can be present but empty (type 2), which must be treated
     * like an absent tag rather than failing the import.
     */
    @Test
    public void testEmptyDateAndTimeDoNotThrow() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.AcquisitionDate, VR.DA, "");
        attributes.setString(Tag.AcquisitionTime, VR.TM, "");
        attributes.setString(Tag.StructureSetDate, VR.DA, "");
        attributes.setString(Tag.StructureSetTime, VR.TM, "");

        Assertions.assertNull(acquisitionStartTimeOf(attributes, "RTSTRUCT"));
    }

    /**
     * An unparsable time must be reported as absent, not propagated as an exception.
     */
    @Test
    public void testUnparsableTimeDoesNotThrow() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.AcquisitionDate, VR.DA, "20180424");
        attributes.setString(Tag.AcquisitionTime, VR.TM, "not-a-time");

        Assertions.assertNull(acquisitionStartTimeOf(attributes, "RTSTRUCT"));
    }

    /**
     * AcquisitionDate/AcquisitionTime stay the preferred source when they are present.
     */
    @Test
    public void testAcquisitionDateAndTimeTakePrecedence() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.AcquisitionDate, VR.DA, "20180424");
        attributes.setString(Tag.AcquisitionTime, VR.TM, "122100");
        attributes.setString(Tag.SeriesDate, VR.DA, "20190101");
        attributes.setString(Tag.SeriesTime, VR.TM, "080000");

        Assertions.assertEquals(LocalDateTime.of(2018, 4, 24, 12, 21, 0),
                acquisitionStartTimeOf(attributes, "RTDOSE"));
    }

    /**
     * SeriesDate/SeriesTime are the last resort, for non-image IODs carrying neither
     * an acquisition, a structure set nor a content date.
     */
    @Test
    public void testFallsBackToSeriesDateAndTime() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.SeriesDate, VR.DA, "20190101");
        attributes.setString(Tag.SeriesTime, VR.TM, "080000");

        Assertions.assertEquals(LocalDateTime.of(2019, 1, 1, 8, 0, 0),
                acquisitionStartTimeOf(attributes, "RTPLAN"));
    }
}
