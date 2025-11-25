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

package org.shanoir.ng.dataset.modality;

import jakarta.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetType;

@Entity
public class BidsDataset extends Dataset {

    private static final long serialVersionUID = 7476089535424633518L;

    /** BIDS data type. */
    private String bidsDataType;

    public BidsDataset() {
    }

    public BidsDataset(Dataset other) {
        super(other);
        this.bidsDataType = ((BidsDataset) other).getBidsDataType();
    }

    @Override
    public DatasetType getType() {
        return DatasetType.BIDS;
    }

    public String getBidsDataType() {
        return bidsDataType;
    }

    public void setBidsDataType(String bidsDataType) {
        this.bidsDataType = bidsDataType;
    }

}
