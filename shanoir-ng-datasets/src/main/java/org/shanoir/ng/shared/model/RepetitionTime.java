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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represents an repetition time. It is used in the MR protocol to
 * list and rank all the repetition times of the acquisition.
 *
 * @author msimon
 *
 */
@Entity
public class RepetitionTime extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -2253233141136120628L;


	/** MR dataset. */
	@ManyToOne
	@JoinColumn(name = "mr_dataset_id")
	@JsonIgnore
	private MrDataset mrDataset;
	
	/**
	 * Comes from the dicom tag (0018,0080) VR=DS, VM=1 Repetition Time. The
	 * unit of measure must be in millisec.
	 */
	@NotNull
	private Double repetitionTimeValue;

	/**
	 * @return the repetitionTimeValue
	 */
	public Double getRepetitionTimeValue() {
		return repetitionTimeValue;
	}

	/**
	 * @param repetitionTimeValue the repetitionTimeValue to set
	 */
	public void setRepetitionTimeValue(Double repetitionTimeValue) {
		this.repetitionTimeValue = repetitionTimeValue;
	}

	public void setMrDataset(MrDataset mrDataset) {
		this.mrDataset = mrDataset;
	}

}
