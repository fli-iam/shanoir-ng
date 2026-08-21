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

package org.shanoir.ng.datasetacquisition.model.xa;

import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

/**
 * XA protocol.
 *
 * @author lvallet
 *
 */
@Entity
public class XaProtocol extends AbstractEntity {

    /**
     * UID
     */
    private static final long serialVersionUID = 3100284961389018913L;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "xaProtocol")
    private XaDatasetAcquisition xaDatasetAcquisition;


    /** (0054, 0081) Number of Slices */
    private Integer numberOfSlices;

    /**
     * (0018,0050) Slice thickness Nominal reconstructed slice thickness, in mm.
     * The unit of measure of the slice thickness must be in mm.
     */
    private Double sliceThickness;

    public XaProtocol() {

    }

    public XaProtocol(XaDatasetAcquisition acq) {
        this.xaDatasetAcquisition = acq;
    }

    /**
     * @return the xaDatasetAcquisition
     */
    public XaDatasetAcquisition getXaDatasetAcquisition() {
        return xaDatasetAcquisition;
    }

    /**
     * @param xaDatasetAcquisition
     *            the xaDatasetAcquisition to set
     */
    public void setXaDatasetAcquisition(XaDatasetAcquisition xaDatasetAcquisition) {
        this.xaDatasetAcquisition = xaDatasetAcquisition;
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
