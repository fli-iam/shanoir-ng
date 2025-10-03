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

package org.shanoir.ng.importer.model;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * This class contains all states of upload process coming from Shanoir Uploader
 * @author mkain
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum UploadState {
    START, //
    START_AUTOIMPORT,
    START_AUTOIMPORT_FAIL,
    MISSING, // Is it used ?
    READY, // Is it used ?
    UPLOADING_IMAGES, // Is it used ?
    UPLOADING_JOB_FILE, // Is it used ?
    FINISHED, //
    ERROR,
    CHECK_OK,
    CHECK_KO; //

    public static UploadState fromString(String value) {
        // Handle modification of FINISHED_UPLOAD state to FINISHED
        if (value.equalsIgnoreCase("FINISHED_UPLOAD")) {
            return UploadState.FINISHED;
        }
        for (UploadState state : UploadState.values()) {
            if (state.name().equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("No UploadState value corresponding to: " + value);
    }
}
