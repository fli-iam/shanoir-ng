package org.shanoir.ng.importer.model;

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
