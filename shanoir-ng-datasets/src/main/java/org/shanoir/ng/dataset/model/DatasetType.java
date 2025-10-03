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

package org.shanoir.ng.dataset.model;

public enum DatasetType {

    CALIBRATION(Names.CALIBRATION),
    CT(Names.CT),
    EEG(Names.EEG),
    MEG(Names.MEG),
    MESH(Names.MESH),
    MR(Names.MR),
    GENERIC(Names.GENERIC),
    PARAMETER_QUANTIFICATION(Names.PARAMETER_QUANTIFICATION),
    PET(Names.PET),
    REGISTRATION(Names.REGISTRATION),
    SEGMENTATION(Names.SEGMENTATION),
    SPECT(Names.SPECT),
    STATISTICAL(Names.STATISTICAL),
    TEMPLATE(Names.TEMPLATE),
    BIDS(Names.BIDS),
    MEASUREMENT(Names.MEASUREMENT),
    XA(Names.XA);

    // this is a hack to use the enum as string values in @JsonSubTypes as it takes no java expressions
    public final class Names {

        private Names() { }

        public static final String CALIBRATION = "Calibration";
        public static final String CT = "Ct";
        public static final String EEG = "Eeg";
        public static final String MEG = "Meg";
        public static final String MESH = "Mesh";
        public static final String MR = "Mr";
        public static final String GENERIC = "Generic";
        public static final String PARAMETER_QUANTIFICATION = "ParameterQuantification";
        public static final String PET = "Pet";
        public static final String REGISTRATION = "Registration";
        public static final String SEGMENTATION = "Segmentation";
        public static final String SPECT = "Spect";
        public static final String STATISTICAL = "Statistical";
        public static final String TEMPLATE = "Template";
        public static final String BIDS = "BIDS";
        public static final String MEASUREMENT = "Measurement";
        public static final String XA = "Xa";
    }

    private final String label;

    private DatasetType(String label) {
        this.label = label;
    }

    public String toString() {
        return this.label;
    }
}
