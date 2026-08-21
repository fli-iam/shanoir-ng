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

package org.shanoir.ng.vip.processingResource.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.core.model.AbstractEntity;

@Entity
public class ProcessingResource extends AbstractEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "processing_id")
    private DatasetProcessing processing;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    @NotNull
    private String resourceId;

    public ProcessingResource() { }

    public ProcessingResource(ExecutionMonitoring processing, Dataset dataset, String resourceId) {
        this.processing = processing;
        this.dataset = dataset;
        this.resourceId = resourceId;
    }

    public DatasetProcessing getProcessing() {
        return processing;
    }

    public void setProcessing(DatasetProcessing processing) {
        this.processing = processing;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
