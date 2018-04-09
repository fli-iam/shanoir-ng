package org.shanoir.ng.shared.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;

/**
 * This class represents a flip angle. It is used in the MR protocol to list and
 * rank all the flip angles of the acquisition.
 *
 * @author msimon
 *
 */
@Entity
public class FlipAngle extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 7894925972778553896L;

	/** MR protocol. */
	@ManyToOne
	@JoinColumn(name = "mr_protocol_id")
	private MrProtocol mrProtocol;


	/** MR dataset. */
	@ManyToOne
	@JoinColumn(name = "mr_dataset_id")
	private MrDataset mrDataset;
	
	/**
	 * Comes from the dicom tag (0018,1314) VR=DS, VM=1 Flip Angle. The unit of
	 * measure must be in millisec.
	 */
	@NotNull
	private Double flipAngleValue;

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
	 * @return the flipAngleValue
	 */
	public Double getFlipAngleValue() {
		return flipAngleValue;
	}

	/**
	 * @param flipAngleValue
	 *            the flipAngleValue to set
	 */
	public void setFlipAngleValue(Double flipAngleValue) {
		this.flipAngleValue = flipAngleValue;
	}

}
