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
