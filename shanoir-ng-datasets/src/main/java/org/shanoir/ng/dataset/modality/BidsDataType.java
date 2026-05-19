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

public enum BidsDataType {

    /* task based and resting state functional MRI.*/
    FUNC("func"),

    /* diffusion weighted imaging.*/
    DWI("dwi"),

    /* field inhomogeneity mapping data such as field maps.*/
    FMAP("fmap"),

    /* structural imaging such as T1, T2, PD, and so on.*/
    ANAT("anat"),

    /* perfusion.*/
    PERF("perf"),

    /* ct.*/
    CT("ct"),

    /* MEG.*/
    MEG("meg"),

    /* EEG.*/
    EEG("eeg"),

    /* IEEG.*/
    IEEG("ieeg"),

    /* Behavioural.*/
    BEH("beh"),

    /* PET.*/
    PET("pet"),

    /* Microscopy.*/
    MICR("micr"),

    /* Near-Infrared Spectroscopy */
    NIRS("nirs"),

    /* X-Ray Angiography */
    XA("xa");

    private String folderName;

    BidsDataType(String string) {
        this.setFolderName(string);
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

}
