package org.shanoir.ng.dataset.modality;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.shared.model.DiffusionGradient;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;

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

	/** Echo time. */
	@ManyToOne
	@JoinColumn(name = "echo_time_id")
	private EchoTime echoTime;

	/** Flip angle. */
	@ManyToOne
	@JoinColumn(name = "flip_angle_id")
	private FlipAngle flipAngle;

	/** Inversion time. */
	@ManyToOne
	@JoinColumn(name = "inversion_time_id")
	private InversionTime inversionTime;

	/** MR Dataset Nature. */
	private Integer mrDatasetNature;

	/** Mr Quality procedure. */
	private Integer mrQualityProcedureType;

	/** Repetition time. */
	@ManyToOne
	@JoinColumn(name = "repetition_time_id")
	private RepetitionTime repetitionTime;

	/** list Diffusion gradients. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrDataset", cascade = CascadeType.ALL)
	private List<DiffusionGradient> diffusionGradients;

	/**
	 * @return the echoTime
	 */
	public EchoTime getEchoTime() {
		return echoTime;
	}

	/**
	 * @param echoTime
	 *            the echoTime to set
	 */
	public void setEchoTime(EchoTime echoTime) {
		this.echoTime = echoTime;
	}

	/**
	 * @return the flipAngle
	 */
	public FlipAngle getFlipAngle() {
		return flipAngle;
	}

	/**
	 * @param flipAngle
	 *            the flipAngle to set
	 */
	public void setFlipAngle(FlipAngle flipAngle) {
		this.flipAngle = flipAngle;
	}

	/**
	 * @return the inversionTime
	 */
	public InversionTime getInversionTime() {
		return inversionTime;
	}

	/**
	 * @param inversionTime
	 *            the inversionTime to set
	 */
	public void setInversionTime(InversionTime inversionTime) {
		this.inversionTime = inversionTime;
	}

	/**
	 * @return the mrDatasetNature
	 */
	public MrDatasetNature getMrDatasetNature() {
		return MrDatasetNature.getNature(mrDatasetNature);
	}

	/**
	 * @param mrDatasetNature
	 *            the mrDatasetNature to set
	 */
	public void setMrDatasetNature(MrDatasetNature mrDatasetNature) {
		if (mrDatasetNature == null) {
			this.mrDatasetNature = null;
		} else {
			this.mrDatasetNature = mrDatasetNature.getId();
		}
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
	 * @return the repetitionTime
	 */
	public RepetitionTime getRepetitionTime() {
		return repetitionTime;
	}

	/**
	 * @param repetitionTime
	 *            the repetitionTime to set
	 */
	public void setRepetitionTime(RepetitionTime repetitionTime) {
		this.repetitionTime = repetitionTime;
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

}
