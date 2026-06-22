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
