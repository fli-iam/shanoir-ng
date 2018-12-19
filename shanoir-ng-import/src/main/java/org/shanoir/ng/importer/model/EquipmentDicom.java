/**
 * 
 */
package org.shanoir.ng.importer.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yyao
 *
 */
public class EquipmentDicom {
	
	@JsonProperty("manufacturer")
	private String manufacturer;

	@JsonProperty("manufacturerModelName")
	private String manufacturerModelName;

	@JsonProperty("deviceSerialNumber")
	private String deviceSerialNumber;
	
	// Keep this empty constructor to avoid Jackson deserialization exceptions
	public EquipmentDicom() {}

	public EquipmentDicom(String manufacturer, String manufacturerModelName, String deviceSerialNumber) {
		this.manufacturer = manufacturer;
		this.manufacturerModelName = manufacturerModelName;
		this.deviceSerialNumber = deviceSerialNumber;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getManufacturerModelName() {
		return manufacturerModelName;
	}

	public void setManufacturerModelName(String manufacturerModelName) {
		this.manufacturerModelName = manufacturerModelName;
	}

	public String getDeviceSerialNumber() {
		return deviceSerialNumber;
	}

	public void setDeviceSerialNumber(String deviceSerialNumber) {
		this.deviceSerialNumber = deviceSerialNumber;
	}
	
	@JsonIgnore
	public boolean isComplete() {
		if (StringUtils.isNotEmpty(this.manufacturer)
			&& StringUtils.isNotEmpty(this.manufacturerModelName)
			&& StringUtils.isNotEmpty(this.deviceSerialNumber)) {
			return true;
		} else {
			return false;
		}
	}

}
