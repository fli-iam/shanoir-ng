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

package org.shanoir.ng.acquisitionequipment;

import org.shanoir.ng.manufacturermodel.ManufacturerModel;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * DTO for acquisition equipments.
 * 
 * @author msimon
 *
 */
public class AcquisitionEquipmentDTO {

	private IdNameDTO center;

	private Long id;

	private ManufacturerModel manufacturerModel;

	private String serialNumber;
	
	private Boolean compatible = false;

	/**
	 * @return the center
	 */
	public IdNameDTO getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter(IdNameDTO center) {
		this.center = center;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the manufacturerModel
	 */
	public ManufacturerModel getManufacturerModel() {
		return manufacturerModel;
	}

	/**
	 * @param manufacturerModel
	 *            the manufacturerModel to set
	 */
	public void setManufacturerModel(ManufacturerModel manufacturerModel) {
		this.manufacturerModel = manufacturerModel;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @param serialNumber
	 *            the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Boolean getCompatible() {
		return compatible;
	}

	public void setCompatible(Boolean compatible) {
		this.compatible = compatible;
	}

}
