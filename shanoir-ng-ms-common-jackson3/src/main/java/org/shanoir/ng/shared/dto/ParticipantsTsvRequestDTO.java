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

package org.shanoir.ng.shared.dto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Request for the participants.tsv of a study: ms datasets -> ms studies.
 * ms studies only knows the birth dates and ms datasets only the examination dates, so ms datasets sends
 * the date of each subject's first examination, the subject ages being computed at that date.
 */
public class ParticipantsTsvRequestDTO {

    private Long studyId;

    /** Date of the first examination of each subject of the study, by subject id */
    private Map<Long, LocalDate> firstExaminationDates = new HashMap<>();

    public ParticipantsTsvRequestDTO() {
        // default constructor
    }

    public ParticipantsTsvRequestDTO(Long studyId, Map<Long, LocalDate> firstExaminationDates) {
        this.studyId = studyId;
        this.firstExaminationDates = firstExaminationDates;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public Map<Long, LocalDate> getFirstExaminationDates() {
        return firstExaminationDates;
    }

    public void setFirstExaminationDates(Map<Long, LocalDate> firstExaminationDates) {
        this.firstExaminationDates = firstExaminationDates;
    }

}
