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

package org.shanoir.uploader.upload;

import jakarta.xml.bind.annotation.XmlType;

/**
 * This class contains all states of the UploadService's
 * state engine, which is responsible to assure a secure
 * upload to the Shanoir server.
 * @author mkain
 *
 */
@XmlType
public enum UploadState {
    START, //
    START_AUTOIMPORT,
    START_AUTOIMPORT_FAIL,
    MISSING, // Is it used ?
    READY, // Is it used ?
    UPLOADING_IMAGES, // Is it used ?
    UPLOADING_JOB_FILE, // Is it used ?
    FINISHED_UPLOAD, //
    ERROR //
}
