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
		return StringUtils.isNotEmpty(this.manufacturer)
			&& StringUtils.isNotEmpty(this.manufacturerModelName)
			&& StringUtils.isNotEmpty(this.deviceSerialNumber);
	}

}
