package org.shanoir.ng.shared.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;

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

	/** MR protocol. */
	@ManyToOne
	@JoinColumn(name = "mr_protocol_id")
	private MrProtocol mrProtocol;

	/** MR dataset. */
	@ManyToOne
	@JoinColumn(name = "mr_dataset_id")
	private MrDataset mrDataset;
	
	/**
	 * Comes from the dicom tag (0018,0080) VR=DS, VM=1 Repetition Time. The
	 * unit of measure must be in millisec.
	 */
	@NotNull
	private Double repetitionTimeValue;

	/**
	 * @return the mrProtocol
	 */
	public MrProtocol getMrProtocol() {
		return mrProtocol;
	}

	/**
	 * @param mrProtocol the mrProtocol to set
	 */
	public void setMrProtocol(MrProtocol mrProtocol) {
		this.mrProtocol = mrProtocol;
	}

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

}
