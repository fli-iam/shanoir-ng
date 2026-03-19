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
package org.shanoir.ng.dataset.model;

import java.util.Set;
import java.util.stream.Collectors;

public class DatasetRightsDTO {

    private Long id;
    private Long centerId;
    private DatasetProcessingDTO datasetProcessing;
    private DatasetAcquisitionDTO datasetAcquisition;
    private Set<StudyIdDTO> relatedStudies;

    public DatasetRightsDTO(Long id,
            Long centerId,
            Long processingStudyId,
            Long acquisitionStudyId,
            Set<StudyIdDTO> relatedStudies) {
        this.id = id;
        this.centerId = centerId;
        this.relatedStudies = relatedStudies;
        if (processingStudyId != null) {
            this.datasetProcessing = new DatasetProcessingDTO(processingStudyId);
        }
        if (acquisitionStudyId != null) {
            this.datasetAcquisition = new DatasetAcquisitionDTO(new ExaminationDTO(acquisitionStudyId));
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCenterId() {
        return centerId;
    }

    public DatasetProcessingDTO getDatasetProcessing() {
        return datasetProcessing;
    }

    public DatasetAcquisitionDTO getDatasetAcquisition() {
        return datasetAcquisition;
    }

    public Set<StudyIdDTO> getRelatedStudies() {
        return relatedStudies;
    }

    public void setRelatedStudies(Set<Long> studyIds) {
        this.relatedStudies = studyIds.stream()
                .map(StudyIdDTO::new)
                .collect(Collectors.toSet());
    }

    // --- nested DTOs ---
    public static class DatasetProcessingDTO {

        private Long studyId;

        public DatasetProcessingDTO(Long studyId) {
            this.studyId = studyId;
        }

        public Long getStudyId() {
            return studyId;
        }
    }

    public static class DatasetAcquisitionDTO {

        private ExaminationDTO examination;

        public DatasetAcquisitionDTO(ExaminationDTO examination) {
            this.examination = examination;
        }

        public ExaminationDTO getExamination() {
            return examination;
        }
    }

    public static class ExaminationDTO {

        private Long studyId;

        public ExaminationDTO(Long studyId) {
            this.studyId = studyId;
        }

        public Long getStudyId() {
            return studyId;
        }
    }

    public static class StudyIdDTO {

        private Long id;

        public StudyIdDTO(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
