/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2025 Inria - https://www.inria.fr/
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

package org.shanoir.ng.dataset.dto;

public class OverallStatisticsDTO {

    private Long studiesCount;
    private Long subjectsCount;
    private Long datasetAcquisitionsCount;
    private Long storageSize;

    public OverallStatisticsDTO(Long studiesCount, Long subjectsCount, Long datasetAcquisitionsCount, Long storageSize) {
        this.studiesCount = studiesCount;
        this.subjectsCount = subjectsCount;
        this.datasetAcquisitionsCount = datasetAcquisitionsCount;
        this.storageSize = storageSize;
    }

    public Long getStudiesCount() {
        return studiesCount;
    }

    public Long getSubjectsCount() {
        return subjectsCount;
    }

    public Long getDatasetAcquisitionsCount() {
        return datasetAcquisitionsCount;
    }

    public Long getStorageSize() {
        return storageSize;
    }
    
}