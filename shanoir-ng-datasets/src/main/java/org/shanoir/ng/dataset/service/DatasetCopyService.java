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
package org.shanoir.ng.dataset.service;

import java.util.Map;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.security.access.prepost.PreAuthorize;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface DatasetCopyService {

    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#dsId, 'CAN_IMPORT'))")
    DatasetCopyResult moveDataset(
            Long dsId,
            Long studyId,
            Map<Long, Long> subjectMap,
            Map<Long, Examination> examMap,
            Map<Long, DatasetAcquisition> acqMap,
            Long userId
    ) throws
            DatasetCopyService.NotFoundSubjectIdException,
            DatasetCopyService.NotFoundDatasetIdException,
            JsonProcessingException;

    public class DatasetCopyResult {

        private final Long newDsId;
        private int countProcessed;
        private int countSuccess;
        private int countCopy;
        private Long examinationId;
        private Long centerId;
        private Long subjectId;


        public DatasetCopyResult(Long newDsId) {
            this.newDsId = newDsId;
        }

        public Long getNewDsId() {
            return newDsId;
        }

        public int getCountProcessed() {
            return countProcessed;
        }

        public int getCountSuccess() {
            return countSuccess;
        }

        public int getCountCopy() {
            return countCopy;
        }

        public void incrementProcessed() {
            this.countProcessed++;
        }

        public void incrementSuccess() {
            this.countSuccess++;
        }

        public void incrementCopy() {
            this.countCopy++;
        }

        public Long getExaminationId() {
            return examinationId;
        }

        public void setExaminationId(Long examinationId) {
            this.examinationId = examinationId;
        }

        public Long getCenterId() {
            return centerId;
        }

        public void setCenterId(Long centerId) {
            this.centerId = centerId;
        }

        public Long getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(Long subjectId) {
            this.subjectId = subjectId;
        }
    }

    public class NotFoundSubjectIdException extends ShanoirException {

        private final Long subjectId;

        public NotFoundSubjectIdException(Long subjectId) {
            super("No mapping found for subject with id = " + subjectId);
            this.subjectId = subjectId;
        }

        public Long getSubjectId() {
            return subjectId;
        }
    }

    public class NotFoundDatasetIdException extends ShanoirException {

        private final Long datasetId;

        public NotFoundDatasetIdException(Long datasetId) {
            super("No dataset found with id = " + datasetId);
            this.datasetId = datasetId;
        }

        public Long getDatasetId() {
            return datasetId;
        }
    }
}
