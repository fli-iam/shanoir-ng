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

package org.shanoir.ng.datasetacquisition.ct;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.shanoir.ng.datasetacquisition.DatasetAcquisition;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * CT dataset acquisition.
 * 
 * @author msimon
 *
 */
@Entity
@JsonTypeName("Ct")
public class CtDatasetAcquisition extends DatasetAcquisition {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -8511002756058790037L;

	/** MR protocol. */
	@OneToOne(cascade = CascadeType.ALL)
	private CtProtocol ctProtocol;

	/**
	 * @return the ctProtocol
	 */
	public CtProtocol getCtProtocol() {
		return ctProtocol;
	}

	/**
	 * @param ctProtocol
	 *            the ctProtocol to set
	 */
	public void setCtProtocol(CtProtocol ctProtocol) {
		this.ctProtocol = ctProtocol;
	}

	@Override
	public String getType() {
		return "Ct";
	}

}
