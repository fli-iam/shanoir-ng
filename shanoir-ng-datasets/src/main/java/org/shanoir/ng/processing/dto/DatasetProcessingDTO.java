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

package org.shanoir.ng.processing.dto;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.DatasetWithProcessingsDTO;
import org.shanoir.ng.processing.model.DatasetProcessingType;

import java.time.LocalDate;
import java.util.List;


/**
 * DTO for dataset.
 *
 * @author msimon
 *
 */
public class DatasetProcessingDTO {

    private Long id;

    private String comment;

    private DatasetProcessingType datasetProcessingType;

    private List<DatasetWithProcessingsDTO> outputDatasets;

    private List<DatasetDTO> inputDatasets;

    private LocalDate processingDate;
    
    private Long studyId;

    private Long parentId;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public DatasetProcessingType getDatasetProcessingType() {
        return datasetProcessingType;
    }

    public void setDatasetProcessingType(DatasetProcessingType datasetProcessingType) {
        this.datasetProcessingType = datasetProcessingType;
    }
    
    public List<DatasetDTO> getInputDatasets() {
        return inputDatasets;
    }

    public void setInputDatasets(List<DatasetDTO> inputDatasets) {
        this.inputDatasets = inputDatasets;
    }

    public List<DatasetWithProcessingsDTO> getOutputDatasets() {
        return outputDatasets;
    }

    public void setOutputDatasets(List<DatasetWithProcessingsDTO> outputDatasets) {
        this.outputDatasets = outputDatasets;
    }

    public LocalDate getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(LocalDate processingDate) {
        this.processingDate = processingDate;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
