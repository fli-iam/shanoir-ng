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

package org.shanoir.ng.datasetacquisition.model.ct;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * CT protocol.
 * 
 * @author msimon
 *
 */
@Entity
public class CtProtocol extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5062475142212117502L;

	/** The MR Dataset acquisition. */
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "ctProtocol")
	private CtDatasetAcquisition ctDatasetAcquisition;

	/**
	 * @return the ctDatasetAcquisition
	 */
	public CtDatasetAcquisition getCtDatasetAcquisition() {
		return ctDatasetAcquisition;
	}

	/**
	 * @param ctDatasetAcquisition
	 *            the ctDatasetAcquisition to set
	 */
	public void setCtDatasetAcquisition(CtDatasetAcquisition ctDatasetAcquisition) {
		this.ctDatasetAcquisition = ctDatasetAcquisition;
	}

}
