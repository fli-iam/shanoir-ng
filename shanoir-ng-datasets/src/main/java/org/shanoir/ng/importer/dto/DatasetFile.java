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

public class DatasetFile {

	@JsonProperty("path")
	private String path;
	
	@JsonProperty("acquisitionNumber")
	private String acquisitionNumber;
	
	@JsonProperty("echoNumbers")
	private List<Integer> echoNumbers;
	
	@JsonProperty("imageOrientationPatient")
	private List<Double> imageOrientationPatient;

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

	public List<Integer> getEchoNumbers() {
		return echoNumbers;
	}

	public void setEchoNumbers(List<Integer> echoNumbers) {
		this.echoNumbers = echoNumbers;
	}

	public List<Double> getImageOrientationPatient() {
		return imageOrientationPatient;
	}

	public void setImageOrientationPatient(List<Double> imageOrientationPatient) {
		this.imageOrientationPatient = imageOrientationPatient;
	}

}
