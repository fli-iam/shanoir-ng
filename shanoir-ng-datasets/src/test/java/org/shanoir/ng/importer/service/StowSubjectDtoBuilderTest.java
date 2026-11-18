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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.Date;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.importer.dto.Subject;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.model.Study;

public class StowSubjectDtoBuilderTest {

    @Test
    public void buildSetsClinicalDefaultsAndTruncatesBirthDate() {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.PatientName, VR.PN, "STOW_TEST");
        Date birth = DateTimeUtils.localDateToDate(LocalDate.of(1978, 6, 15));
        attributes.setDate(Tag.PatientBirthDate, VR.DA, birth);
        attributes.setString(Tag.PatientSex, VR.CS, "f");

        Study study = new Study();
        study.setId(42L);
        study.setName("bench-study");

        Subject dto = StowSubjectDtoBuilder.build(attributes, study);

        assertEquals("STOW_TEST", dto.getName());
        assertEquals(42L, dto.getStudy().getId());
        assertEquals(LocalDate.of(1978, 1, 1), dto.getBirthDate());
        assertEquals(2, dto.getSex());
        assertEquals("LIVING_HUMAN_BEING", dto.getImagedObjectCategory());
        assertEquals("PATIENT", dto.getSubjectType());
    }

    @Test
    public void mapPatientSexMapsDicomCodes() {
        assertEquals(1, StowSubjectDtoBuilder.mapPatientSex("M"));
        assertEquals(2, StowSubjectDtoBuilder.mapPatientSex("f"));
        assertEquals(0, StowSubjectDtoBuilder.mapPatientSex("O"));
        assertNull(StowSubjectDtoBuilder.mapPatientSex("  "));
    }

    @Test
    public void buildLeavesBirthDateNullWhenAbsent() {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.PatientName, VR.PN, "NO_DOB");

        Study study = new Study();
        study.setId(1L);
        study.setName("s");

        Subject dto = StowSubjectDtoBuilder.build(attributes, study);

        assertNull(dto.getBirthDate());
        assertNull(dto.getSex());
        assertEquals("PATIENT", dto.getSubjectType());
    }
}
