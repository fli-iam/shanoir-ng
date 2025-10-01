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

package org.shanoir.ng.model;

public enum NiftiConverter {

    DCM2NII_2008_03_31(1, "/opt/nifti-converters/dcm2nii_2008-03-31"),

    MCVERTER_2_0_7(2, "/opt/nifti-converters/mcverter_2.0.7"),

    DCM2NII_2014_08_04(4, "/opt/nifti-converters/dcm2nii_2014-08-04"),

    MCVERTER_2_1_0(5, "/opt/nifti-converters/mcverter_2.1.0"),

    DCM2NIIX(6, "dcm2niix"),

    DICOMIFIER(7, "dicomifier"),

    MRICONVERTER(8, "/opt/nifti-converters/mriconverter/MRIFileManager/MRIManager.jar");

    private final int id;

    private final String path;

    /**
     * Constructor.
     *
     * @param id
     *            id
     */
    private NiftiConverter(final int id, final String path) {
        this.id = id;
        this.path = path;
    }

    /**
     * Get a type by its id.
     *
     * @param id
     *            format id.
     * @return type of converter
     */
    public static NiftiConverter getType(final Integer id) {
        if (id == null) {
            return null;
        }
        for (NiftiConverter type : NiftiConverter.values()) {
            if (id.equals(type.getId())) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching type for id " + id);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    public String getPath() { return path; }
}
