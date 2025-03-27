package org.shanoir.ng.dataset.modality;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetType;

import jakarta.persistence.Entity;

@Entity
public class GenericDataset extends Dataset {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -5363216669486303309L;

    public GenericDataset() {

    }

    public GenericDataset(Dataset other) {
        super(other);
    }

    @Override
    public DatasetType getType() {
        return DatasetType.Generic;
    }

}
