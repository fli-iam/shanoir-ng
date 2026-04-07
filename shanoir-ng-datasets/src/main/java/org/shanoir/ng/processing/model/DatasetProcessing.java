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

package org.shanoir.ng.processing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dataset Processing.
 *
 * @author msimon
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class DatasetProcessing extends AbstractEntity {

    /**
     * UID
     */
    private static final long serialVersionUID = 9196056506956939617L;

    /**
     * A comment on the dataset processing . Could be the command line of the
     * processing.
     */
    private String comment;

    /** Dataset Processing Type. */
    private Integer datasetProcessingType;

    /** Input datasets. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "INPUT_OF_DATASET_PROCESSING", joinColumns = @JoinColumn(name = "PROCESSING_ID"), inverseJoinColumns = @JoinColumn(name = "DATASET_ID"))
    private List<Dataset> inputDatasets;

    /** Output Dataset List. */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "datasetProcessing")
    private List<Dataset> outputDatasets;

    /** Date of the dataset processing. */
    @LocalDateAnnotations
    private LocalDate processingDate;

    /** The study for which this dataset is a result. */
    @NotNull
    private Long studyId;

    /** Parent dataset processing id (e.g. VIP execution) **/
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private DatasetProcessing parent;

    /** Authenticated user that created the processing */
    private String username;

    public DatasetProcessing() {

    }

    public DatasetProcessing(DatasetProcessing dproc) {
        this.comment = dproc.getComment();
        if (dproc.getDatasetProcessingType() != null) {
            this.datasetProcessingType = dproc.getDatasetProcessingType().getId();
        } else {
            this.datasetProcessingType = null;
        }
        this.inputDatasets = dproc.getInputDatasets();
        this.outputDatasets = dproc.getOutputDatasets();
        this.processingDate = dproc.getProcessingDate();
        this.studyId = dproc.getStudyId();
        this.parent = dproc.getParent();
        this.username = dproc.getUsername();
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the datasetProcessingType
     */
    public DatasetProcessingType getDatasetProcessingType() {
        return DatasetProcessingType.getType(datasetProcessingType);
    }

    /**
     * @param datasetProcessingType the datasetProcessingType to set
     */
    public void setDatasetProcessingType(DatasetProcessingType datasetProcessingType) {
        if (datasetProcessingType == null) {
            this.datasetProcessingType = null;
        } else {
            this.datasetProcessingType = datasetProcessingType.getId();
        }
    }

    /**
     * @return the inputDatasets
     */
    public List<Dataset> getInputDatasets() {
        return inputDatasets;
    }

    /**
     * @param inputDatasets the inputDatasets to set
     */
    public void setInputDatasets(List<Dataset> inputDatasets) {
        this.inputDatasets = inputDatasets;
    }

    /**
     * @return the outputDatasets
     */
    public List<Dataset> getOutputDatasets() {
        return outputDatasets;
    }

    /**
     * @param outputDatasets the outputDatasets to set
     */
    public void setOutputDatasets(List<Dataset> outputDatasets) {
        this.outputDatasets = outputDatasets;
    }

    /**
     * @param outputDataset the outputDataset to add
     */
    public void addOutputDataset(Dataset outputDataset) {
        if (this.outputDatasets == null) {
            this.outputDatasets = new ArrayList<Dataset>();
        }
        this.outputDatasets.add(outputDataset);
    }

    /**
     * @return the processingDate
     */
    public LocalDate getProcessingDate() {
        return processingDate;
    }

    /**
     * @param processingDate the processingDate to set
     */
    public void setProcessingDate(LocalDate processingDate) {
        this.processingDate = processingDate;
    }

    /**
     * @return the studyId
     */
    public Long getStudyId() {
        return studyId;
    }

    /**
     * @param studyId the studyId to set
     */
    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public DatasetProcessing getParent() {
        return parent;
    }

    public void setParent(DatasetProcessing parent) {
        this.parent = parent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
