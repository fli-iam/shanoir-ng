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

import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

/**
 * CT protocol.
 *
 * @author msimon
 *
 */
@Entity
public class CtProtocol extends AbstractEntity {

    /**
     * UID
     */
    private static final long serialVersionUID = 5062475142212117502L;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "ctProtocol")
    private CtDatasetAcquisition ctDatasetAcquisition;

    /** (0054, 0081) Number of Slices */
    private Integer numberOfSlices;

    /**
     * The unit of measure of the slice thickness must be in mm.
     */
    private Double sliceThickness;

    public CtProtocol() {

    }

    public CtProtocol(CtDatasetAcquisition acq) {
        this.ctDatasetAcquisition = acq;
    }

    /**
     * @return the ctDatasetAcquisition
     */
    public CtDatasetAcquisition getCtDatasetAcquisition() {
        return ctDatasetAcquisition;
    }

    /**
     * @param ctDatasetAcquisition
     *            the ctDatasetAcquisition to set
     */
    public void setCtDatasetAcquisition(CtDatasetAcquisition ctDatasetAcquisition) {
        this.ctDatasetAcquisition = ctDatasetAcquisition;
    }

    public Integer getNumberOfSlices() {
        return numberOfSlices;
    }

    public void setNumberOfSlices(Integer numberOfSlices) {
        this.numberOfSlices = numberOfSlices;
    }

    public Double getSliceThickness() {
        return sliceThickness;
    }

    public void setSliceThickness(Double sliceThickness) {
        this.sliceThickness = sliceThickness;
    }
}
