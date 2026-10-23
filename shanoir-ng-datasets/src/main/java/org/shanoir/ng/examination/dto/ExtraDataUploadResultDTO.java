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

package org.shanoir.ng.examination.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a multi-file extra data upload, on an examination or a dataset acquisition.
 */
public class ExtraDataUploadResultDTO {

    /** Files successfully stored. */
    private List<String> uploaded = new ArrayList<>();

    /** Files not stored because a file with the same name already exists. */
    private List<String> skipped = new ArrayList<>();

    /** Files not stored because of a storage error. */
    private List<String> failed = new ArrayList<>();

    public List<String> getUploaded() {
        return uploaded;
    }

    public void setUploaded(List<String> uploaded) {
        this.uploaded = uploaded;
    }

    public List<String> getSkipped() {
        return skipped;
    }

    public void setSkipped(List<String> skipped) {
        this.skipped = skipped;
    }

    public List<String> getFailed() {
        return failed;
    }

    public void setFailed(List<String> failed) {
        this.failed = failed;
    }

}
