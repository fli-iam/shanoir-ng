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

package org.shanoir.ng.datasetacquisition.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.Entity;

@Entity
@JsonTypeName("Generic")
public class GenericDatasetAcquisition extends DatasetAcquisition {

    public static final String DATASET_ACQUISITION_TYPE = "Generic";
    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -8826440216825057112L;

    public GenericDatasetAcquisition() {
    }

    public GenericDatasetAcquisition(DatasetAcquisition other) {
        super(other);
    }

    @Override
    public String getType() {
        return "Generic";
    }

}
