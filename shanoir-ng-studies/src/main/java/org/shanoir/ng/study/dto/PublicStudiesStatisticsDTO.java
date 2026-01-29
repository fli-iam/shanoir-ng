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

package org.shanoir.ng.study.dto;

/**
 * DTO for public statistics.
 *
 * @author lvallet
 *
 */
public class PublicStudiesStatisticsDTO {

    private Long studies;

    private Long storageVolume;

    private Long subjects;

    private Long examinations;

    public Long getStudies() {
        return studies;
    }

    public void setStudies(Long studies) {
        this.studies = studies;
    }

    public Long getStorageVolume() {
        return storageVolume;
    }

    public void setStorageVolume(Long storageVolume) {
        this.storageVolume = storageVolume;
    }

    public Long getSubjects() {
        return subjects;
    }

    public void setSubjects(Long subjects) {
        this.subjects = subjects;
    }

    public Long getExaminations() {
        return examinations;
    }

    public void setExaminations(Long examinations) {
        this.examinations = examinations;
    }

}
