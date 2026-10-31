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

package org.shanoir.ng.importer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Test;

class DicomExaminationMetadataResolverTest {

    @Test
    void resolveExaminationDatePrefersStudyDate() {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.StudyDate, VR.DA, "20170313");
        attributes.setString(Tag.SeriesDate, VR.DA, "20170314");

        assertEquals(LocalDate.of(2017, 3, 13), DicomExaminationMetadataResolver.resolveExaminationDate(attributes));
    }

    @Test
    void resolveExaminationDateFallsBackToSeriesDate() {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.SeriesDate, VR.DA, "20170314");

        assertEquals(LocalDate.of(2017, 3, 14), DicomExaminationMetadataResolver.resolveExaminationDate(attributes));
    }

    @Test
    void resolveExaminationDateFallsBackToAcquisitionThenContentDate() {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.ContentDate, VR.DA, "20170316");
        assertEquals(LocalDate.of(2017, 3, 16), DicomExaminationMetadataResolver.resolveExaminationDate(attributes));
        attributes.setString(Tag.AcquisitionDate, VR.DA, "20170315");
        assertEquals(LocalDate.of(2017, 3, 15), DicomExaminationMetadataResolver.resolveExaminationDate(attributes));
    }

    @Test
    void resolveExaminationDateNullWithoutAnyDate() {
        assertNull(DicomExaminationMetadataResolver.resolveExaminationDate(new Attributes()));
    }

    @Test
    void resolveCommentReturnsStudyDescription() {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.StudyDescription, VR.LO, "THORAX ABDO");

        assertEquals("THORAX ABDO", DicomExaminationMetadataResolver.resolveComment(attributes));
    }

    @Test
    void resolveCommentNullWhenAbsent() {
        assertNull(DicomExaminationMetadataResolver.resolveComment(new Attributes()));
    }
}
