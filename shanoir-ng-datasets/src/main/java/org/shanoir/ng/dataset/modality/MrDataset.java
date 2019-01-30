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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.shared.model.DiffusionGradient;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;
import javax.persistence.Transient;

/**
 * MR dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class MrDataset extends Dataset {

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
	@OneToOne
	private MrDatasetMetadata originMrMetadata;

	/** Repetition time. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrDataset", cascade = CascadeType.ALL)
	private List<RepetitionTime> repetitionTime;

	/** Metadata updated by study card. */
	@OneToOne
	private MrDatasetMetadata updatedMrMetadata;
	
	/** Store temporarily the first image acquisition time until all images are processed*/
	@Transient
	private  Date firstImageAcquisitionTime;

	/** Store temporarily the last image acquisition time until all images are processed */
	@Transient	
	private Date lastImageAcquisitionTime;
	
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
			this.echoTime =  new ArrayList<EchoTime>();
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
			this.flipAngle = new ArrayList<FlipAngle>();
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
			this.inversionTime =  new ArrayList<InversionTime>();
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
			this.repetitionTime = new ArrayList<RepetitionTime>();
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

	public Date getFirstImageAcquisitionTime() {
		return firstImageAcquisitionTime;
	}

	public void setFirstImageAcquisitionTime(Date firstImageAcquisitionTime) {
		this.firstImageAcquisitionTime = firstImageAcquisitionTime;
	}

	public Date getLastImageAcquisitionTime() {
		return lastImageAcquisitionTime;
	}

	public void setLastImageAcquisitionTime(Date lastImageAcquisitionTime) {
		this.lastImageAcquisitionTime = lastImageAcquisitionTime;
	}
	


}
