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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represents a diffusion gradient. It is used in the MR protocol to
 * list and rank all the diffusion gradients of the acquisition.
 * 
 * @author msimon
 *
 */
@Entity
public class DiffusionGradient extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -426008108821946235L;

	/** MR protocol. */
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "mr_protocol_id")
	private MrProtocol mrProtocol;

	/** MR dataset. */
	@JsonIgnore
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
