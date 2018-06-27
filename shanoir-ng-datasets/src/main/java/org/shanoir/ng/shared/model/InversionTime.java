package org.shanoir.ng.shared.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;

/**
 * This class represents an inversion time. It is used in the MR protocol to
 * list and rank all the inversion times of the acquisition.
 *
 * @author msimon
 *
 */
@Entity
public class InversionTime extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -4243060486957154039L;

	/** MR dataset. */
	@ManyToOne
	@JoinColumn(name = "mr_dataset_id")
	private MrDataset mrDataset;
	
	/**
	 * Comes from the dicom tag (0018,0082) VR=DS, VM=1 Inversion Time. The unit
	 * of measure must be in millisec.
	 */
	@NotNull
	private Double inversionTimeValue;

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

}
