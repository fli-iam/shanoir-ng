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

package org.shanoir.ng.datasetacquisition.model.xa;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

/**
 * XA dataset acquisition.
 * 
 * @author lvallet
 *
 */
@Entity
@JsonTypeName("Xa")
public class XaDatasetAcquisition extends DatasetAcquisition {

	public static final String DATASET_ACQUISITION_TYPE = "Xa";

	/**
	 * UID
	 */
	private static final long serialVersionUID = -1525419756058748021L;

	@OneToOne(cascade = CascadeType.ALL)
	private XaProtocol xaProtocol;

	public XaDatasetAcquisition() {
	}

	public XaDatasetAcquisition(DatasetAcquisition other) {
		super(other);
		this.xaProtocol = new XaProtocol(this);
	}

	/**
	 * @return the xaProtocol
	 */
	public XaProtocol getXaProtocol() {
		return xaProtocol;
	}

	/**
	 * @param xaProtocol
	 *            the xaProtocol to set
	 */
	public void setXaProtocol(XaProtocol xaProtocol) {
		this.xaProtocol = xaProtocol;
	}

	@Override
	public String getType() {
		return "Xa";
	}

}
