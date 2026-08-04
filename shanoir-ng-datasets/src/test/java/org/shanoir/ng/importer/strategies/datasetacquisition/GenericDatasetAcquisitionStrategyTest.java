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
 * Tests null-safe acquisition start time handling of GenericDatasetAcquisitionStrategy.
 */
public class GenericDatasetAcquisitionStrategyTest {

    private static final String USER_NAME = "test";

    private final GenericDatasetAcquisitionStrategy strategy = new GenericDatasetAcquisitionStrategy();

    private Serie generateSerie() {
        Serie serie = new Serie();
        serie.setModality("OT");
        serie.setSeriesInstanceUID("1.2.3.4.5");
        serie.setSeriesDescription("test serie");
        return serie;
    }

    private LocalDateTime acquisitionStartTimeOf(Attributes attributes) throws Exception {
        DatasetAcquisition acquisition = strategy.generateFlatDatasetAcquisitionForSerie(
                USER_NAME, generateSerie(), 0, attributes);
        return acquisition.getAcquisitionStartTime();
    }

    @Test
    public void testMissingAcquisitionDateDoesNotThrow() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.SeriesDescription, VR.LO, "generic serie");

        Assertions.assertNull(acquisitionStartTimeOf(attributes));
    }

    @Test
    public void testEmptyDateAndTimeDoNotThrow() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.AcquisitionDate, VR.DA, "");
        attributes.setString(Tag.AcquisitionTime, VR.TM, "");

        Assertions.assertNull(acquisitionStartTimeOf(attributes));
    }

    @Test
    public void testAcquisitionDateAndTimeAreParsed() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.AcquisitionDate, VR.DA, "20180424");
        attributes.setString(Tag.AcquisitionTime, VR.TM, "122100");

        Assertions.assertEquals(LocalDateTime.of(2018, 4, 24, 12, 21, 0),
                acquisitionStartTimeOf(attributes));
    }

    @Test
    public void testUnparsableTimeDoesNotThrow() throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.AcquisitionDate, VR.DA, "20180424");
        attributes.setString(Tag.AcquisitionTime, VR.TM, "not-a-time");

        Assertions.assertNull(acquisitionStartTimeOf(attributes));
    }
}
