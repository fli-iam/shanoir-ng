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

/**
 * Study and center of an examination: ms datasets -> other microservices (preclinical),
 * to check the study rights of a user, restricted to some centers or not, on data attached to an examination.
 */
public class ExaminationStudyCenterDTO {

    private Long studyId;

    private Long centerId;

    public ExaminationStudyCenterDTO() {
        // default constructor
    }

    public ExaminationStudyCenterDTO(Long studyId, Long centerId) {
        this.studyId = studyId;
        this.centerId = centerId;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

}
