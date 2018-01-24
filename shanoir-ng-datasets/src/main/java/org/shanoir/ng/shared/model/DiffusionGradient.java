package org.shanoir.ng.shared.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;

/**
 * This class represents a diffusion gradient. It is used in the MR protocol to
 * list and rank all the diffusion gradients of the acquisition.
 * 
 * @author msimon
 *
 */
@Entity
public class DiffusionGradient extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -426008108821946235L;

	/** MR protocol. */
	@ManyToOne
	@JoinColumn(name = "mr_protocol_id")
	private MrProtocol mrProtocol;

	/** MR dataset. */
	@ManyToOne
	@JoinColumn(name = "mr_dataset_id")
	private MrDataset mrDataset;

	/** The B value. */
	@NotNull
	private Double diffusionGradientBValue;

	/** Orientation along X. */
	@NotNull
	private Double diffusionGradientOrientationX;

	/** Orientation along Y. */
	@NotNull
	private Double diffusionGradientOrientationY;

	/** Orientation along Z. */
	@NotNull
	private Double diffusionGradientOrientationZ;

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
	 * @return the mrDataset
	 */
	public MrDataset getMrDataset() {
		return mrDataset;
	}

	/**
	 * @param mrDataset
	 *            the mrDataset to set
	 */
	public void setMrDataset(MrDataset mrDataset) {
		this.mrDataset = mrDataset;
	}

	/**
	 * @return the diffusionGradientBValue
	 */
	public Double getDiffusionGradientBValue() {
		return diffusionGradientBValue;
	}

	/**
	 * @param diffusionGradientBValue
	 *            the diffusionGradientBValue to set
	 */
	public void setDiffusionGradientBValue(Double diffusionGradientBValue) {
		this.diffusionGradientBValue = diffusionGradientBValue;
	}

	/**
	 * @return the diffusionGradientOrientationX
	 */
	public Double getDiffusionGradientOrientationX() {
		return diffusionGradientOrientationX;
	}

	/**
	 * @param diffusionGradientOrientationX
	 *            the diffusionGradientOrientationX to set
	 */
	public void setDiffusionGradientOrientationX(Double diffusionGradientOrientationX) {
		this.diffusionGradientOrientationX = diffusionGradientOrientationX;
	}

	/**
	 * @return the diffusionGradientOrientationY
	 */
	public Double getDiffusionGradientOrientationY() {
		return diffusionGradientOrientationY;
	}

	/**
	 * @param diffusionGradientOrientationY
	 *            the diffusionGradientOrientationY to set
	 */
	public void setDiffusionGradientOrientationY(Double diffusionGradientOrientationY) {
		this.diffusionGradientOrientationY = diffusionGradientOrientationY;
	}

	/**
	 * @return the diffusionGradientOrientationZ
	 */
	public Double getDiffusionGradientOrientationZ() {
		return diffusionGradientOrientationZ;
	}

	/**
	 * @param diffusionGradientOrientationZ
	 *            the diffusionGradientOrientationZ to set
	 */
	public void setDiffusionGradientOrientationZ(Double diffusionGradientOrientationZ) {
		this.diffusionGradientOrientationZ = diffusionGradientOrientationZ;
	}

}
