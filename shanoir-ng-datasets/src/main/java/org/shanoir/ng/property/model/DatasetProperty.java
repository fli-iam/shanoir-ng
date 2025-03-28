package org.shanoir.ng.property.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.core.model.AbstractEntity;

@Entity
public class DatasetProperty extends AbstractEntity {

    private static final long serialVersionUID = 7602484872590225134L;

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    @ManyToOne
    @JoinColumn(name = "dataset_processing_id")
    private DatasetProcessing processing;

    private String name;

    private String value;

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public DatasetProcessing getProcessing() {
        return processing;
    }

    public void setProcessing(DatasetProcessing processing) {
        this.processing = processing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
