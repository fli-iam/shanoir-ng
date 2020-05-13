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

package org.shanoir.ng.importer.dto;

import org.shanoir.ng.shared.core.model.IdName;

/**
 * DTO for coil.
 * 
 * @author msimon
 */
public class CoilDTO {

	private IdName center;

	private CoilType coilType;
	
	private Long id;

	private IdName manufacturerModel;

	private String name;

	private Long numberOfChannels;

	private String serialNumber;

	/**
	 * Default constructor.
	 */
	public CoilDTO() {
		// This default constructor is empty
	}
	
	/**
	 * @return the center
	 */
	public IdName getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(IdName center) {
		this.center = center;
	}

	/**
	 * @return the coilType
	 */
	public CoilType getCoilType() {
		return coilType;
	}

	/**
	 * @param coilType the coilType to set
	 */
	public void setCoilType(CoilType coilType) {
		this.coilType = coilType;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}



	/**
	 * @return the manufacturerModel
	 */
	public IdName getManufacturerModel() {
		return manufacturerModel;
	}

	/**
	 * @param manufacturerModel the manufacturerModel to set
	 */
	public void setManufacturerModel(IdName manufacturerModel) {
		this.manufacturerModel = manufacturerModel;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the numberOfChannels
	 */
	public Long getNumberOfChannels() {
		return numberOfChannels;
	}

	/**
	 * @param numberOfChannels the numberOfChannels to set
	 */
	public void setNumberOfChannels(Long numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @param serialNumber the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}
