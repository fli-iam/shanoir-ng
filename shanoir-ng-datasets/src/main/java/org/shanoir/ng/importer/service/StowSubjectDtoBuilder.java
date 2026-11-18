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

import java.time.LocalDate;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.importer.dto.Subject;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.model.Study;

/**
 * Builds the RabbitMQ subject payload for STOW-RS imports (MS Studies queue).
 * Aligns with ZIP import conventions: birth date truncated to 1 January of the year,
 * plus category/type so the UI can open the subject without mandatory edits.
 */
final class StowSubjectDtoBuilder {

    private StowSubjectDtoBuilder() {
    }

    static Subject build(Attributes attributes, Study study) {
        Subject subjectDTO = new Subject();
        subjectDTO.setName(attributes.getString(Tag.PatientName));
        subjectDTO.setStudy(new IdName(study.getId(), study.getName()));

        Date patientBirthDate = attributes.getDate(Tag.PatientBirthDate);
        LocalDate birthDate = DateTimeUtils.dateToLocalDate(patientBirthDate);
        if (birthDate != null) {
            subjectDTO.setBirthDate(birthDate.withDayOfYear(1));
        }

        Integer sexId = mapPatientSex(attributes.getString(Tag.PatientSex));
        if (sexId != null) {
            subjectDTO.setSex(sexId);
        }

        subjectDTO.setImagedObjectCategory("LIVING_HUMAN_BEING");
        subjectDTO.setSubjectType("PATIENT");
        return subjectDTO;
    }

    /**
     * DICOM Patient Sex (0010,0040) to studies {@code Sex} id: M=1, F=2, other/unknown=0.
     */
    static Integer mapPatientSex(String patientSex) {
        if (StringUtils.isBlank(patientSex)) {
            return null;
        }
        char code = patientSex.trim().toUpperCase().charAt(0);
        switch (code) {
            case 'M':
                return 1;
            case 'F':
                return 2;
            default:
                return 0;
        }
    }
}
