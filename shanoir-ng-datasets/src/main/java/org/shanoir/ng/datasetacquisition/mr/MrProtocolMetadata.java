package org.shanoir.ng.datasetacquisition.mr;

import javax.persistence.Entity;

import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * MR protocol.
 * 
 * @author msimon
 *
 */
@Entity
public class MrProtocolMetadata extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -5850582993918561681L;

	/**
	 * (0008,0008) Image Type or (0008, 9209) Acquisition contrast (see Dicom p
	 * 682 et al).
	 */
	private Integer acquisitionContrast;

	/**
	 * The axis orientation at acquisition. To be entered with the study card
	 * mechanism.
	 */
	private Integer axisOrientationAtAcquisition;

	/** A comment for adding other detais. */
	private String comment;

	/**
	 * (0018,1049) Contrast/Bolus Ingredient Concentration. The unit of measure
	 * of injected volume must be in ml.
	 */
	private Double contrastAgentConcentration;

	/** The manufactured name for the contrast agent. */
	private String contrastAgentProduct;

	/**
	 * From (0018,0010) Contrast or bolus agent and (0018,1048) Contrast/bolus
	 * ingredient.
	 */
	private Integer contrastAgentUsed;

	/**
	 * Volume : volume injected in milliliters of diluted contrast agent. The
	 * unit of measure of injected volume must be in ml.
	 */
	private Double injectedVolume;

	/**
	 * From (0018,0021) Sequence variant where you can find MTC (standing for
	 * Magnetization Transfer Contrast).
	 */
	private Boolean magnetizationTransfer;

	/**
	 * Corresponds to the third semantic axis of ontoNeurolog (ontology of MR
	 * sequences).
	 */
	private Integer mrSequenceApplication;

	/**
	 * Derived from (0018,0091) Echo train length (Number of lines in k- space
	 * acquired per excitation per image) and from the (0018,9032) Geometry
	 * category of k-Space traversal.
	 */
	private Integer mrSequenceKSpaceFill;

	/** Corresponding Dicom information : (0018,0024) Sequence name. */
	private String mrSequenceName;

	/**
	 * Derived from (0018, 0020) Scanning sequence Description of the type of
	 * data taken and (0018, 0021) Sequence Variant of the scanning sequence. To
	 * be entered using the "study card" mechanism.
	 */
	private Integer mrSequencePhysics;

	/** Corresponding Dicom information : (0018,1030) Protocol name. */
	private String name;

	/** From (0018,1041) Parallel acquisition Contrast/Bolus. */
	private Boolean parallelAcquisition;

	/**
	 * From dicom tag (0018,9078) VR=CS, VM=1 Parallel Acquisition Technique.
	 */
	private Integer parallelAcquisitionTechnique;

	/** Receiving coil. */
	private Long receivingCoilId;

	/** From (0018, 9151) Frame reference Date time. */
	private Integer sliceOrder;

	/** Cannot be easily found from DICOM data elements. */
	private Integer sliceOrientationAtAcquisition;

	/**
	 * From dicom tag (0018,9069) VR=FD, VM=1 Parallel Reduction Factor
	 * In-plane.
	 */
	private Double timeReductionFactorForTheInPlaneDirection;

	/**
	 * From dicom tag (0018,9155) VR=FD, VM=1 Parallel Reduction Factor
	 * out-of-plane.
	 */
	private Double timeReductionFactorForTheOutOfPlaneDirection;

	/** Transmitting coil. */
	private Long transmittingCoilId;

	/**
	 * @return the acquisitionContrast
	 */
	public AcquisitionContrast getAcquisitionContrast() {
		return AcquisitionContrast.getContrast(acquisitionContrast);
	}

	/**
	 * @param acquisitionContrast
	 *            the acquisitionContrast to set
	 */
	public void setAcquisitionContrast(AcquisitionContrast acquisitionContrast) {
		if (acquisitionContrast == null) {
			this.acquisitionContrast = null;
		} else {
			this.acquisitionContrast = acquisitionContrast.getId();
		}
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
	 * @return the contrastAgentConcentration
	 */
	public Double getContrastAgentConcentration() {
		return contrastAgentConcentration;
	}

	/**
	 * @param contrastAgentConcentration
	 *            the contrastAgentConcentration to set
	 */
	public void setContrastAgentConcentration(Double contrastAgentConcentration) {
		this.contrastAgentConcentration = contrastAgentConcentration;
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
	 * @return the contrastAgentUsed
	 */
	public ContrastAgentUsed getContrastAgentUsed() {
		return ContrastAgentUsed.getConstrastAgent(contrastAgentUsed);
	}

	/**
	 * @param contrastAgentUsed
	 *            the contrastAgentUsed to set
	 */
	public void setContrastAgentUsed(ContrastAgentUsed contrastAgentUsed) {
		if (contrastAgentUsed == null) {
			this.contrastAgentUsed = null;
		} else {
			this.contrastAgentUsed = contrastAgentUsed.getId();
		}
	}

	/**
	 * @return the injectedVolume
	 */
	public Double getInjectedVolume() {
		return injectedVolume;
	}

	/**
	 * @param injectedVolume
	 *            the injectedVolume to set
	 */
	public void setInjectedVolume(Double injectedVolume) {
		this.injectedVolume = injectedVolume;
	}

	/**
	 * @return the magnetizationTransfer
	 */
	public Boolean getMagnetizationTransfer() {
		return magnetizationTransfer;
	}

	/**
	 * @param magnetizationTransfer
	 *            the magnetizationTransfer to set
	 */
	public void setMagnetizationTransfer(Boolean magnetizationTransfer) {
		this.magnetizationTransfer = magnetizationTransfer;
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
	 * @return the mrSequenceKSpaceFill
	 */
	public MrSequenceKSpaceFill getMrSequenceKSpaceFill() {
		return MrSequenceKSpaceFill.getKSpaceFill(mrSequenceKSpaceFill);
	}

	/**
	 * @param mrSequenceKSpaceFill
	 *            the mrSequenceKSpaceFill to set
	 */
	public void setMrSequenceKSpaceFill(MrSequenceKSpaceFill mrSequenceKSpaceFill) {
		if (mrSequenceKSpaceFill == null) {
			this.mrSequenceKSpaceFill = null;
		} else {
			this.mrSequenceKSpaceFill = mrSequenceKSpaceFill.getId();
		}
	}

	/**
	 * @return the mrSequenceName
	 */
	public String getMrSequenceName() {
		return mrSequenceName;
	}

	/**
	 * @param mrSequenceName
	 *            the mrSequenceName to set
	 */
	public void setMrSequenceName(String mrSequenceName) {
		this.mrSequenceName = mrSequenceName;
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

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parallelAcquisition
	 */
	public Boolean getParallelAcquisition() {
		return parallelAcquisition;
	}

	/**
	 * @param parallelAcquisition
	 *            the parallelAcquisition to set
	 */
	public void setParallelAcquisition(Boolean parallelAcquisition) {
		this.parallelAcquisition = parallelAcquisition;
	}

	/**
	 * @return the parallelAcquisitionTechnique
	 */
	public ParallelAcquisitionTechnique getParallelAcquisitionTechnique() {
		return ParallelAcquisitionTechnique.getTechnique(parallelAcquisitionTechnique);
	}

	/**
	 * @param parallelAcquisitionTechnique
	 *            the parallelAcquisitionTechnique to set
	 */
	public void setParallelAcquisitionTechnique(ParallelAcquisitionTechnique parallelAcquisitionTechnique) {
		if (parallelAcquisitionTechnique == null) {
			this.parallelAcquisitionTechnique = null;
		} else {
			this.parallelAcquisitionTechnique = parallelAcquisitionTechnique.getId();
		}
	}

	/**
	 * @return the receivingCoilId
	 */
	public Long getReceivingCoilId() {
		return receivingCoilId;
	}

	/**
	 * @param receivingCoilId
	 *            the receivingCoilId to set
	 */
	public void setReceivingCoilId(Long receivingCoilId) {
		this.receivingCoilId = receivingCoilId;
	}

	/**
	 * @return the sliceOrder
	 */
	public SliceOrder getSliceOrder() {
		return SliceOrder.getOrder(sliceOrder);
	}

	/**
	 * @param sliceOrder
	 *            the sliceOrder to set
	 */
	public void setSliceOrder(SliceOrder sliceOrder) {
		if (sliceOrder == null) {
			this.sliceOrder = null;
		} else {
			this.sliceOrder = sliceOrder.getId();
		}
	}

	/**
	 * @return the sliceOrientationAtAcquisition
	 */
	public SliceOrientationAtAcquisition getSliceOrientationAtAcquisition() {
		return SliceOrientationAtAcquisition.getOrientation(sliceOrientationAtAcquisition);
	}

	/**
	 * @param sliceOrientationAtAcquisition
	 *            the sliceOrientationAtAcquisition to set
	 */
	public void setSliceOrientationAtAcquisition(SliceOrientationAtAcquisition sliceOrientationAtAcquisition) {
		if (sliceOrientationAtAcquisition == null) {
			this.sliceOrientationAtAcquisition = null;
		} else {
			this.sliceOrientationAtAcquisition = sliceOrientationAtAcquisition.getId();
		}
	}

	/**
	 * @return the timeReductionFactorForTheInPlaneDirection
	 */
	public Double getTimeReductionFactorForTheInPlaneDirection() {
		return timeReductionFactorForTheInPlaneDirection;
	}

	/**
	 * @param timeReductionFactorForTheInPlaneDirection
	 *            the timeReductionFactorForTheInPlaneDirection to set
	 */
	public void setTimeReductionFactorForTheInPlaneDirection(Double timeReductionFactorForTheInPlaneDirection) {
		this.timeReductionFactorForTheInPlaneDirection = timeReductionFactorForTheInPlaneDirection;
	}

	/**
	 * @return the timeReductionFactorForTheOutOfPlaneDirection
	 */
	public Double getTimeReductionFactorForTheOutOfPlaneDirection() {
		return timeReductionFactorForTheOutOfPlaneDirection;
	}

	/**
	 * @param timeReductionFactorForTheOutOfPlaneDirection
	 *            the timeReductionFactorForTheOutOfPlaneDirection to set
	 */
	public void setTimeReductionFactorForTheOutOfPlaneDirection(Double timeReductionFactorForTheOutOfPlaneDirection) {
		this.timeReductionFactorForTheOutOfPlaneDirection = timeReductionFactorForTheOutOfPlaneDirection;
	}

	/**
	 * @return the transmittingCoilId
	 */
	public Long getTransmittingCoilId() {
		return transmittingCoilId;
	}

	/**
	 * @param transmittingCoilId
	 *            the transmittingCoilId to set
	 */
	public void setTransmittingCoilId(Long transmittingCoilId) {
		this.transmittingCoilId = transmittingCoilId;
	}

}
