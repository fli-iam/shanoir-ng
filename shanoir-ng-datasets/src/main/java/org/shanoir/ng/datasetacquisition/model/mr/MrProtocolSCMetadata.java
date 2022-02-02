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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.shanoir.ng.dataset.modality.BidsDataType;

@Entity
@DiscriminatorValue("2")
public class MrProtocolSCMetadata extends MrProtocolMetadata {


	/** A comment for adding other detais. */
	private String comment;
	
	/**
	 * The axis orientation at acquisition. To be entered with the study card
	 * mechanism.
	 */
	private Integer axisOrientationAtAcquisition;

	/** The manufactured name for the contrast agent. */
	private String contrastAgentProduct;
	
	/**
	 * Corresponds to the third semantic axis of ontoNeurolog (ontology of MR
	 * sequences).
	 */
	private Integer mrSequenceApplication;
	
	/**
	 * Derived from (0018, 0020) Scanning sequence Description of the type of
	 * data taken and (0018, 0021) Sequence Variant of the scanning sequence. 
	 */
	private Integer mrSequencePhysics;
	
	/** Bids dataset type folder. **/
	private String bidsDataType;

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * @return the axisOrientationAtAcquisition
	 */
	public AxisOrientationAtAcquisition getAxisOrientationAtAcquisition() {
		return AxisOrientationAtAcquisition.getAxisOrientation(axisOrientationAtAcquisition);
	}

	/**
	 * @param axisOrientationAtAcquisition
	 *            the axisOrientationAtAcquisition to set
	 */
	public void setAxisOrientationAtAcquisition(AxisOrientationAtAcquisition axisOrientationAtAcquisition) {
		if (axisOrientationAtAcquisition == null) {
			this.axisOrientationAtAcquisition = null;
		} else {
			this.axisOrientationAtAcquisition = axisOrientationAtAcquisition.getId();
		}
	}

	/**
	 * @return the contrastAgentProduct
	 */
	public String getContrastAgentProduct() {
		return contrastAgentProduct;
	}

	/**
	 * @param contrastAgentProduct
	 *            the contrastAgentProduct to set
	 */
	public void setContrastAgentProduct(String contrastAgentProduct) {
		this.contrastAgentProduct = contrastAgentProduct;
	}
	
	/**
	 * @return the mrSequenceApplication
	 */
	public MrSequenceApplication getMrSequenceApplication() {
		return MrSequenceApplication.getApplication(mrSequenceApplication);
	}

	/**
	 * @param mrSequenceApplication
	 *            the mrSequenceApplication to set
	 */
	public void setMrSequenceApplication(MrSequenceApplication mrSequenceApplication) {
		if (mrSequenceApplication == null) {
			this.mrSequenceApplication = null;
		} else {
			this.mrSequenceApplication = mrSequenceApplication.getId();
		}
	}
	
	/**
	 * @return the mrSequencePhysics
	 */
	public MrSequencePhysics getMrSequencePhysics() {
		return MrSequencePhysics.getPhysics(mrSequencePhysics);
	}

	/**
	 * @param mrSequencePhysics
	 *            the mrSequencePhysics to set
	 */
	public void setMrSequencePhysics(MrSequencePhysics mrSequencePhysics) {
		if (mrSequencePhysics == null) {
			this.mrSequencePhysics = null;
		} else {
			this.mrSequencePhysics = mrSequencePhysics.getId();
		}
	}

	public String getBidsDataType() {
		return bidsDataType;
	}

	public void setBidsDataType(String bidsDataType) {
		this.bidsDataType = bidsDataType;
	}

	public void setBidsDataType(BidsDataType bidsDataType) {
		this.bidsDataType = bidsDataType.getFolderName();
	}
}
