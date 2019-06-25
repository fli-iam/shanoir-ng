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

package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * Registration dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class RegistrationDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -2435422041970785779L;

	/** Registration Dataset Nature. */
	private Integer registrationDatasetType;

	/**
	 * @return the registrationDatasetType
	 */
	public RegistrationDatasetType getRegistrationDatasetType() {
		return RegistrationDatasetType.getType(registrationDatasetType);
	}

	/**
	 * @param registrationDatasetType
	 *            the registrationDatasetType to set
	 */
	public void setRegistrationDatasetType(RegistrationDatasetType registrationDatasetType) {
		if (registrationDatasetType == null) {
			this.registrationDatasetType = null;
		} else {
			this.registrationDatasetType = registrationDatasetType.getId();
		}
	}

	@Override
	public String getType() {
		return "Registration";
	}

}
