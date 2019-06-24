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

package org.shanoir.ng.importer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {

	@JsonProperty("path")
	public String path;

	@JsonProperty("acquisitionNumber")
	public String acquisitionNumber;

	@JsonProperty("echoTimes")
	public List<EchoTime> echoTimes;
	
	@JsonProperty("repetitionTime")
	public Double repetitionTime;
	
	@JsonProperty("inversionTime")
	public Double inversionTime;

	@JsonProperty("flipAngle")
	public String flipAngle;

	@JsonProperty("imageOrientationPatient")
	public List<Double> imageOrientationPatient;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAcquisitionNumber() {
		return acquisitionNumber;
	}

	public void setAcquisitionNumber(String acquisitionNumber) {
		this.acquisitionNumber = acquisitionNumber;
	}

	public List<Double> getImageOrientationPatient() {
		return imageOrientationPatient;
	}

	public void setImageOrientationPatient(List<Double> imageOrientationPatient) {
		this.imageOrientationPatient = imageOrientationPatient;
	}

	public List<EchoTime> getEchoTimes() {
		return echoTimes;
	}

	public void setEchoTimes(List<EchoTime> echoTimes) {
		this.echoTimes = echoTimes;
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
	
	

}