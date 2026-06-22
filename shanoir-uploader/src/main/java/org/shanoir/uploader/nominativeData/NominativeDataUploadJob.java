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

package org.shanoir.uploader.nominativeData;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.shanoir.uploader.upload.UploadState;

/**
 * This class contains Nominative date that will be displayed in the current
 * uploads tab of ShanoirUploader.
 *
 * @author ifakhfakh
 *
 */
@XmlType
@XmlRootElement
public class NominativeDataUploadJob {

    private String patientPseudonymusHash;

    private String patientName;

    private String ipp;

    private String studyDate;

    private String mriSerialNumber;

    private String uploadPercentage;

    private UploadState uploadState;

    public String getPatientPseudonymusHash() {
        return patientPseudonymusHash;
    }

    public void setPatientPseudonymusHash(String patientPseudonymusHash) {
        this.patientPseudonymusHash = patientPseudonymusHash;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getIPP() {
        return ipp;
    }

    public void setIPP(String iPP) {
        ipp = iPP;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getMriSerialNumber() {
        return mriSerialNumber;
    }

    public void setMriSerialNumber(String mriSerialNumber) {
        this.mriSerialNumber = mriSerialNumber;
    }

    public String getUploadPercentage() {
        return uploadPercentage;
    }

    public void setUploadPercentage(String uploadPercentage) {
        this.uploadPercentage = uploadPercentage;
    }

    public UploadState getUploadState() {
        return uploadState;
    }

    public void setUploadState(UploadState uploadState) {
        this.uploadState = uploadState;
    }

}
