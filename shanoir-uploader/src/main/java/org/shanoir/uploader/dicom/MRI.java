package org.shanoir.uploader.dicom;

/**
 * The Class MRI contains information about the MRI Acquisition.
 *
 * @author ifakhfakh
 */

public class MRI {
	
	private String manufacturer;
	private String stationName;
	private String deviceSerialNumber;
	private String manufacturersModelName;
	private String institutionName;
	private String institutionAddress;
	
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public String getDeviceSerialNumber() {
		return deviceSerialNumber;
	}
	public void setDeviceSerialNumber(String deviceSerialNumber) {
		this.deviceSerialNumber = deviceSerialNumber;
	}
	public String getManufacturersModelName() {
		return manufacturersModelName;
	}
	public void setManufacturersModelName(String manufacturersModelName) {
		this.manufacturersModelName = manufacturersModelName;
	}
	public String getInstitutionName() {
		return institutionName;
	}
	public void setInstitutionName(String institutionName) {
		this.institutionName = institutionName;
	}
	public String getInstitutionAddress() {
		return institutionAddress;
	}
	public void setInstitutionAddress(String institutionAddress) {
		this.institutionAddress = institutionAddress;
	}

}
