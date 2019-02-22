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

package org.shanoir.ng.coil;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.center.Center;
import org.shanoir.ng.manufacturermodel.ManufacturerModel;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Coil.
 * 
 * @author msimon
 */
@Entity
@JsonPropertyOrder({ "_links", "id" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class Coil extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5779678698062107549L;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "center_id")
	private Center center;

	/** Coil type from dicom tag (0018, 9051) or (0018,9043). */
	private CoilType coilType;

	@ManyToOne
	@JoinColumn(name = "manufacturer_model_id")
	private ManufacturerModel manufacturerModel;

	@NotNull
	private String name;

	private Long numberOfChannels;

	private String serialNumber;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "coil/" + getId());
	}

	/**
	 * @return the center
	 */
	public Center getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter(Center center) {
		this.center = center;
	}

	/**
	 * @return the coilType
	 */
	public CoilType getCoilType() {
		return coilType;
	}

	/**
	 * @param coilType
	 *            the coilType to set
	 */
	public void setCoilType(CoilType coilType) {
		this.coilType = coilType;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
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
	 * @param numberOfChannels
	 *            the numberOfChannels to set
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
	 * @param serialNumber
	 *            the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}
