package org.shanoir.ng.study;

/**
 * DTO equipment becoming from Dicom file.
 * 
 * @author msimon
 *
 */
public class EquipmentDicom {

	private String deviceSerialNumber;
	private String manufacturer;
	private String manufacturerModelName;

	/**
	 * @return the deviceSerialNumber
	 */
	public String getDeviceSerialNumber() {
		return deviceSerialNumber;
	}

	/**
	 * @param deviceSerialNumber
	 *            the deviceSerialNumber to set
	 */
	public void setDeviceSerialNumber(String deviceSerialNumber) {
		this.deviceSerialNumber = deviceSerialNumber;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @param manufacturer
	 *            the manufacturer to set
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	/**
	 * @return the manufacturerModelName
	 */
	public String getManufacturerModelName() {
		return manufacturerModelName;
	}

	/**
	 * @param manufacturerModelName
	 *            the manufacturerModelName to set
	 */
	public void setManufacturerModelName(String manufacturerModelName) {
		this.manufacturerModelName = manufacturerModelName;
	}

}
