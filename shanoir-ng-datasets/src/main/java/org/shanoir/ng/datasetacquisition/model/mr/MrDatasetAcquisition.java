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

package org.shanoir.ng.datasetacquisition.model.mr;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

/**
 * MR dataset acquisition.
 * 
 * @author msimon
 *
 */
@Entity
@JsonTypeName("Mr")
public class MrDatasetAcquisition extends DatasetAcquisition {

	public static final String datasetAcquisitionType = "Mr";
	/**
	 * UID
	 */
	private static final long serialVersionUID = 2532815427171578710L;

	/** MR protocol. */
	@OneToOne(cascade = CascadeType.ALL)
	private MrProtocol mrProtocol;

	public MrDatasetAcquisition() {
	}

	public MrDatasetAcquisition(DatasetAcquisition acq, MrDataset mrDs) {
		super(acq);
		this.mrProtocol = new MrProtocol(((MrDatasetAcquisition) acq).getMrProtocol(), mrDs);
	}

	/**
	 * @return the mrProtocol
	 */
	public MrProtocol getMrProtocol() {
		return mrProtocol;
	}

	/**
	 * @param mrProtocol
	 *            the mrProtocol to set
	 */
	public void setMrProtocol(MrProtocol mrProtocol) {
		this.mrProtocol = mrProtocol;
	}

	@Override
	public String getType() {
		return "Mr";
	}

}
