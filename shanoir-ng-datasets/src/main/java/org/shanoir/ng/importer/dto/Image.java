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