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

package org.shanoir.ng.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Test;

/**
 * The per-dataset MR timing sets the ZIP import fills from every image, a STOW-RS import
 * takes from the instance that creates the dataset.
 */
public class DicomImporterServiceMrParametersTest {

    @Test
    public void repetitionInversionAndFlipAngleComeFromTheInstance() {
        Attributes attributes = new Attributes();
        attributes.setDouble(Tag.RepetitionTime, VR.DS, 2300.0);
        attributes.setDouble(Tag.InversionTime, VR.DS, 900.0);
        attributes.setString(Tag.FlipAngle, VR.DS, "9");
        org.shanoir.ng.importer.dto.Dataset dataset = new org.shanoir.ng.importer.dto.Dataset();

        DicomImporterService.addMrTimingParameters(dataset, attributes);

        assertEquals(Set.of(2300.0), dataset.getRepetitionTimes());
        assertEquals(Set.of(900.0), dataset.getInversionTimes());
        assertEquals(Set.of("9"), dataset.getFlipAngles());
    }

    /** The ZIP import records 0 for a tag the image does not have; so does this. */
    @Test
    public void absentTagsGiveTheZipImportsDefaults() {
        org.shanoir.ng.importer.dto.Dataset dataset = new org.shanoir.ng.importer.dto.Dataset();

        DicomImporterService.addMrTimingParameters(dataset, new Attributes());

        assertEquals(Set.of(0.0), dataset.getRepetitionTimes());
        assertEquals(Set.of(0.0), dataset.getInversionTimes());
        assertEquals(Set.of("0"), dataset.getFlipAngles());
    }

}
