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

package org.shanoir.ng.dicom.web.service;

public class DICOMWebSTOWRSResultInstance {

    private String sopInstanceUID;

    private String sopClassUID;

    private String status;

    private Integer failureReason;

    private String failureReasonText;

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String uid) {
        this.sopInstanceUID = uid;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public void setSopClassUID(String uid) {
        this.sopClassUID = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(Integer reason) {
        this.failureReason = reason;
    }

    public String getFailureReasonText() {
        return failureReasonText;
    }

    public void setFailureReasonText(String text) {
        this.failureReasonText = text;
    }

    public boolean isDuplicate() {
        // 0xB306 (45830) = Instance already stored
        // 0xB305 (45829) = Instance received but not stored
        return failureReason != null
                && (failureReason == 0xB306 || failureReason == 45830
                        || failureReason == 0xB305 || failureReason == 45829);
    }

}
