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
