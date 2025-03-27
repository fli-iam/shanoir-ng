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

    Calibration(Names.Calibration),
    Ct(Names.Ct),
    Eeg(Names.Eeg),
    Meg(Names.Meg),
    Mesh(Names.Mesh),
    Mr(Names.Mr),
    Generic(Names.Generic),
    ParameterQuantification(Names.ParameterQuantification),
    Pet(Names.Pet),
    Registration(Names.Registration),
    Segmentation(Names.Segmentation),
    Spect(Names.Spect),
    Statistical(Names.Statistical),
    Template(Names.Template),
    BIDS(Names.BIDS),
    Measurement(Names.Measurement),
    Xa(Names.Xa);

    // this is a hack to use the enum as string values in @JsonSubTypes as it takes no java expressions
    public class Names{
        public static final String Calibration = "Calibration";
        public static final String Ct = "Ct";
        public static final String Eeg = "Eeg";
        public static final String Meg = "Meg";
        public static final String Mesh = "Mesh";
        public static final String Mr = "Mr";
        public static final String Generic = "Generic";
        public static final String ParameterQuantification = "ParameterQuantification";
        public static final String Pet = "Pet";
        public static final String Registration = "Registration";
        public static final String Segmentation = "Segmentation";
        public static final String Spect = "Spect";
        public static final String Statistical = "Statistical";
        public static final String Template = "Template";
        public static final String BIDS = "BIDS";
        public static final String Measurement = "Measurement";
        public static final String Xa = "Xa";
    }

    private final String label;

    private DatasetType(String label) {
        this.label = label;
    }

    public String toString() {
        return this.label;
    }
}
