package org.shanoir.uploader.model.dto;

public class EquipmentDicom {
	
	private String manufacturer;
	private String manufacturerModelName;
	private String deviceSerialNumber;

	public EquipmentDicom(String manufacturer, String manufacturerModelName, String deviceSerialNumber) {
		super();
		this.manufacturer = manufacturer;
		this.manufacturerModelName = manufacturerModelName;
		this.deviceSerialNumber = deviceSerialNumber;
	}

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

	public String payload() {
		String payload = "";
		if (getDeviceSerialNumber() != null) {
			payload += "{\"deviceSerialNumber\": \"" + getDeviceSerialNumber() + "\",";
		} else {
			payload += "{\"deviceSerialNumber\": \"\",";
		}

		if (getManufacturer() != null) {
			payload += "\"manufacturer\": \"" + getManufacturer() + "\",";
		} else {
			payload += "\"manufacturer\": \"\",";
		}

		if (getManufacturerModelName() != null) {
			payload += "\"manufacturerModelName\": \"" + getManufacturerModelName() + "\"}";
		} else {
			payload += "\"manufacturerModelName\": \"\"}";
		}
		return payload;
	}
}
