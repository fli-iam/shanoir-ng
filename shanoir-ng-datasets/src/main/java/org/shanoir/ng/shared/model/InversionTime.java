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

package org.shanoir.ng.shared.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.examination.model.UnitOfMeasure;
import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represents an inversion time. It is used in the MR protocol to
 * list and rank all the inversion times of the acquisition.
 *
 * @author msimon
 *
 */
@Entity
public class InversionTime extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -4243060486957154039L;

	/** MR dataset. */
	@ManyToOne
	@JoinColumn(name = "mr_dataset_id")
	@JsonIgnore
	private MrDataset mrDataset;
	
	/**
	 * Comes from the dicom tag (0018,0082) VR=DS, VM=1 Inversion Time. The unit
	 * of measure must be in millisec.
	 */
	@NotNull
	private Double inversionTimeValue;

	@Transient
	@JsonInclude
	private UnitOfMeasure unit = UnitOfMeasure.MS;

	/**
	 * @return the inversionTimeValue
	 */
	public Double getInversionTimeValue() {
		return inversionTimeValue;
	}

	/**
	 * @param inversionTimeValue
	 *            the inversionTimeValue to set
	 */
	public void setInversionTimeValue(Double inversionTimeValue) {
		this.inversionTimeValue = inversionTimeValue;
	}

	public MrDataset getMrDataset() {
		return mrDataset;
	}

	public void setMrDataset(MrDataset mrDataset) {
		this.mrDataset = mrDataset;
	}

	public UnitOfMeasure getUnit() {
		return unit;
	}

	public void setUnit(UnitOfMeasure unit) {
		this.unit = unit;
	}
}
