package org.shanoir.ng.importer.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {

	@JsonProperty("path")
	public String path;

	@JsonProperty("acquisitionNumber")
	public int acquisitionNumber;

	@JsonProperty("echoTimes")
	public Set<EchoTime> echoTimes;
	
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

}
