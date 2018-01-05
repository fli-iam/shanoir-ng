package org.shanoir.ng.shared.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.datasetacquisition.mr.MrProtocol;
import org.shanoir.ng.shared.model.AbstractGenericItem;

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

	/** MR protocol. */
	@ManyToOne
	@JoinColumn(name = "mr_protocol_id")
	private MrProtocol mrProtocol;

	/**
	 * Comes from the dicom tag (0018,0082) VR=DS, VM=1 Inversion Time. The unit
	 * of measure must be in millisec.
	 */
	@NotNull
	private Double inversionTimeValue;

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

}
