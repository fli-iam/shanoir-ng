package org.shanoir.uploader.model.dto.rest.importmodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetFile {

	@JsonProperty("path")
	private String path;
	
	@JsonProperty("acquisitionNumber")
	private int acquisitionNumber;
	
	@JsonProperty("imageOrientationPatient")
	private List<Double> imageOrientationPatient;

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

	public void setImageOrientationPatient(List<Double> imageOrientationPatient) {
		this.imageOrientationPatient = imageOrientationPatient;
	}

}
