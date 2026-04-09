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

package org.shanoir.ng.datasetacquisition.model.ct;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

/**
 * CT dataset acquisition.
 *
 * @author msimon
 *
 */
@Entity
@JsonTypeName("Ct")
public class CtDatasetAcquisition extends DatasetAcquisition {

    public static final String DATASET_ACQUISITION_TYPE = "Ct";

    /**
     * UID
     */
    private static final long serialVersionUID = -8511002756058790037L;

    @OneToOne(cascade = CascadeType.ALL)
    private CtProtocol ctProtocol;

    public CtDatasetAcquisition() {
    }

    public CtDatasetAcquisition(DatasetAcquisition other) {
        super(other);
        this.ctProtocol = new CtProtocol(this);
    }

    /**
     * @return the ctProtocol
     */
    public CtProtocol getCtProtocol() {
        return ctProtocol;
    }

    /**
     * @param ctProtocol
     *            the ctProtocol to set
     */
    public void setCtProtocol(CtProtocol ctProtocol) {
        this.ctProtocol = ctProtocol;
    }

    @Override
    public String getType() {
        return "Ct";
    }

}
