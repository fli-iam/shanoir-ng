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

import java.util.List;
import java.util.Set;

import org.shanoir.ng.shared.dicom.EchoTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {

    @JsonProperty("path")
    private String path;

    @JsonProperty("acquisitionNumber")
    private int acquisitionNumber;

    @JsonProperty("echoTimes")
    private Set<EchoTime> echoTimes;

    @JsonProperty("repetitionTime")
    private Double repetitionTime;

    @JsonProperty("inversionTime")
    private Double inversionTime;

    @JsonProperty("flipAngle")
    private String flipAngle;

    @JsonProperty("imageOrientationPatient")
    private List<Double> imageOrientationPatient;

    private String sopInstanceUID;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getAcquisitionNumber() {
        return acquisitionNumber;
    }

    public void setAcquisitionNumber(int acquisitionNumber) {
        this.acquisitionNumber = acquisitionNumber;
    }

    public List<Double> getImageOrientationPatient() {
        return imageOrientationPatient;
    }

    public Set<EchoTime> getEchoTimes() {
        return echoTimes;
    }

    public void setEchoTimes(Set<EchoTime> echoTimes) {
        this.echoTimes = echoTimes;
    }

    public void setImageOrientationPatient(List<Double> imageOrientationPatient) {
        this.imageOrientationPatient = imageOrientationPatient;
    }

    public Double getRepetitionTime() {
        return repetitionTime;
    }

    public void setRepetitionTime(Double repetitionTime) {
        this.repetitionTime = repetitionTime;
    }

    public Double getInversionTime() {
        return inversionTime;
    }

    public void setInversionTime(Double inversionTime) {
        this.inversionTime = inversionTime;
    }

    public String getFlipAngle() {
        return flipAngle;
    }

    public void setFlipAngle(String flipAngle) {
        this.flipAngle = flipAngle;
    }

    public String getSOPInstanceUID() {
        return sopInstanceUID;
    }

    public void setSOPInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }
}
