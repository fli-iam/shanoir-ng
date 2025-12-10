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

package org.shanoir.uploader.model.rest;

public class ManufacturerModel {

    private Long id;

    private String datasetModalityType;

    private Double magneticField;

    private Manufacturer manufacturer;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the magneticField
     */
    public Double getMagneticField() {
        return magneticField;
    }

    @Override
    public String toString() {
        return manufacturer.toString() + " " + name + " (" + magneticField + ")";
    }

    /**
     * @param magneticField
     *            the magneticField to set
     */
    public void setMagneticField(Double magneticField) {
        this.magneticField = magneticField;
    }

    /**
     * @return the manufacturer
     */
    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    /**
     * @param manufacturer
     *            the manufacturer to set
     */
    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getDatasetModalityType() {
        return datasetModalityType;
    }

    public void setDatasetModalityType(String datasetModalityType) {
        this.datasetModalityType = datasetModalityType;
    }

}
