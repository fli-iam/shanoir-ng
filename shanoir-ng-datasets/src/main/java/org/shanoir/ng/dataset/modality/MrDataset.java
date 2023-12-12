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

package org.shanoir.ng.dataset.modality;

import jakarta.persistence.*;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.shared.model.*;
import org.xmlunit.diff.Diff;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MR dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class MrDataset extends Dataset {

	public static final String datasetType = "Mr";

	/**
	 * UID
	 */
	private static final long serialVersionUID = 6801936202135911035L;

	/** list Diffusion gradients. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrDataset", cascade = CascadeType.ALL)
	private List<DiffusionGradient> diffusionGradients;

	/** Echo time. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrDataset", cascade = CascadeType.ALL)
	private List<EchoTime> echoTime;

	/** Flip angle. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrDataset", cascade = CascadeType.ALL)
	private List<FlipAngle> flipAngle;

	/** Inversion time. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrDataset", cascade = CascadeType.ALL)
	private List<InversionTime> inversionTime;

	/** Mr Quality procedure. */
	private Integer mrQualityProcedureType;

	/** Origin metadata. */
	@OneToOne(cascade = CascadeType.ALL)
	private MrDatasetMetadata originMrMetadata;

	/** Repetition time. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrDataset", cascade = CascadeType.ALL)
	private List<RepetitionTime> repetitionTime;

	/** Metadata updated by study card. */
	@OneToOne(cascade = CascadeType.ALL)
	private MrDatasetMetadata updatedMrMetadata;
	
	/** Store temporarily the first image acquisition time until all images are processed*/
	@Transient
	private LocalDateTime  firstImageAcquisitionTime;

	/** Store temporarily the last image acquisition time until all images are processed */
	@Transient
	private LocalDateTime lastImageAcquisitionTime;

	public MrDataset() {}

	public MrDataset(Dataset d, MrProtocol mrpro) {
		super(d);

		MrProtocol mrp = new MrProtocol(mrpro, this);
		this.diffusionGradients = new ArrayList<>(((MrDataset) d).getDiffusionGradients().size());
		for (DiffusionGradient dg : ((MrDataset) d).getDiffusionGradients()) {
			this.diffusionGradients.add(new DiffusionGradient(dg, this, mrp));
		}

		this.echoTime = new ArrayList<>(((MrDataset) d).getEchoTime().size());
		for (EchoTime et : ((MrDataset) d).getEchoTime()) {
			this.echoTime.add(new EchoTime(et, this));
		}

		this.flipAngle = new ArrayList<>(((MrDataset) d).getFlipAngle().size());
		for (FlipAngle fa : ((MrDataset) d).getFlipAngle()) {
			this.flipAngle.add(new FlipAngle(fa, this));
		}

		this.inversionTime = new ArrayList<>(((MrDataset) d).getInversionTime().size());
		for (InversionTime it : ((MrDataset) d).getInversionTime()) {
			this.inversionTime.add(new InversionTime(it, this));
		}

		if (((MrDataset) d).getMrQualityProcedureType() != null) {
			this.mrQualityProcedureType = ((MrDataset) d).getMrQualityProcedureType().getId();
		} else {
			this.mrQualityProcedureType = null;
		}
		this.originMrMetadata = ((MrDataset) d).getOriginMrMetadata();

		this.repetitionTime = new ArrayList<>(((MrDataset) d).getRepetitionTime().size());
		for (RepetitionTime rt : ((MrDataset) d).getRepetitionTime()) {
			this.repetitionTime.add(new RepetitionTime(rt, this));
		}
		this.updatedMrMetadata = new MrDatasetMetadata(((MrDataset) d).getUpdatedMrMetadata());
		this.firstImageAcquisitionTime = ((MrDataset) d).getFirstImageAcquisitionTime();
		this.lastImageAcquisitionTime = ((MrDataset) d).getLastImageAcquisitionTime();
	}

	/**
	 * @return the diffusionGradients
	 */
	public List<DiffusionGradient> getDiffusionGradients() {
		return diffusionGradients;
	}

	/**
	 * @param diffusionGradients
	 *            the diffusionGradients to set
	 */
	public void setDiffusionGradients(List<DiffusionGradient> diffusionGradients) {
		this.diffusionGradients = diffusionGradients;
	}

	/**
	 * @return the echoTime
	 */
	public List<EchoTime> getEchoTime() {
		if (echoTime == null) {
			this.echoTime =  new ArrayList<>();
		}
		return echoTime;
	}

	/**
	 * @param echoTime
	 *            the echoTime to set
	 */
	public void setEchoTime(List<EchoTime> echoTimes) {
		this.echoTime = echoTimes;
	}

	/**
	 * @return the flipAngle
	 */
	public List<FlipAngle> getFlipAngle() {
		if (flipAngle == null) {
			this.flipAngle = new ArrayList<>();
		}
		return flipAngle;
	}

	/**
	 * @param flipAngle
	 *            the flipAngle to set
	 */
	public void setFlipAngle(List<FlipAngle> flipAngles) {
		this.flipAngle = flipAngles;
	}

	/**
	 * @return the inversionTime
	 */
	public List<InversionTime> getInversionTime() {
		if (inversionTime == null) {
			this.inversionTime =  new ArrayList<>();
		}
		return inversionTime;
	}

	/**
	 * @param inversionTime
	 *            the inversionTime to set
	 */
	public void setInversionTime(List<InversionTime> inversionTimes) {
		this.inversionTime = inversionTimes;
	}

	/**
	 * @return the mrQualityProcedureType
	 */
	public MrQualityProcedureType getMrQualityProcedureType() {
		return MrQualityProcedureType.getType(mrQualityProcedureType);
	}

	/**
	 * @param mrQualityProcedureType
	 *            the mrQualityProcedureType to set
	 */
	public void setMrQualityProcedureType(MrQualityProcedureType mrQualityProcedureType) {
		if (mrQualityProcedureType == null) {
			this.mrQualityProcedureType = null;
		} else {
			this.mrQualityProcedureType = mrQualityProcedureType.getId();
		}
	}

	/**
	 * @return the originMrMetadata
	 */
	public MrDatasetMetadata getOriginMrMetadata() {
		return originMrMetadata;
	}

	/**
	 * @param originMrMetadata
	 *            the originMrMetadata to set
	 */
	public void setOriginMrMetadata(MrDatasetMetadata originMrMetadata) {
		this.originMrMetadata = originMrMetadata;
	}

	/**
	 * @return the repetitionTime
	 */
	public List<RepetitionTime> getRepetitionTime() {
		if (repetitionTime == null) {
			this.repetitionTime = new ArrayList<>();
		}
		return repetitionTime;
	}

	/**
	 * @param repetitionTime
	 *            the repetitionTime to set
	 */
	public void setRepetitionTime(List<RepetitionTime> repetitionTimes) {
		this.repetitionTime = repetitionTimes;
	}

	@Override
	public String getType() {
		return "Mr";
	}

	/**
	 * @return the updatedMrMetadata
	 */
	public MrDatasetMetadata getUpdatedMrMetadata() {
		return updatedMrMetadata;
	}

	/**
	 * @param updatedMrMetadata
	 *            the updatedMrMetadata to set
	 */
	public void setUpdatedMrMetadata(MrDatasetMetadata updatedMrMetadata) {
		this.updatedMrMetadata = updatedMrMetadata;
	}

	public LocalDateTime getFirstImageAcquisitionTime() {
		return firstImageAcquisitionTime;
	}

	public void setFirstImageAcquisitionTime(LocalDateTime firstImageAcquisitionTime) {
		this.firstImageAcquisitionTime = firstImageAcquisitionTime;
	}

	public LocalDateTime getLastImageAcquisitionTime() {
		return lastImageAcquisitionTime;
	}

	public void setLastImageAcquisitionTime(LocalDateTime lastImageAcquisitionTime) {
		this.lastImageAcquisitionTime = lastImageAcquisitionTime;
	}

}
