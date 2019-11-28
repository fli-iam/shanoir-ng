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

package org.shanoir.ng.datasetacquisition.model.pet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PET dataset acquisition.
 * 
 * @author msimon
 *
 */
@Entity
@JsonTypeName("Pet")
public class PetDatasetAcquisition extends DatasetAcquisition {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -7819600575968825257L;

	/** PET protocol. */
	@OneToOne(cascade = CascadeType.ALL)
	private PetProtocol petProtocol;

	/**
	 * @return the petProtocol
	 */
	public PetProtocol getPetProtocol() {
		return petProtocol;
	}

	/**
	 * @param petProtocol
	 *            the petProtocol to set
	 */
	public void setPetProtocol(PetProtocol petProtocol) {
		this.petProtocol = petProtocol;
	}

	@Override
	public String getType() {
		return "Pet";
	}

}
