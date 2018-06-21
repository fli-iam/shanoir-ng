package org.shanoir.ng.datasetacquisition.mr;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;


import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * MR protocol.
 * 
 * @author msimon
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="dtype", 
discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("1")
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
	 * (0018,1049) Contrast/Bolus Ingredient Concentration. The unit of measure
	 * of injected volume must be in ml.
	 */
	private Double contrastAgentConcentration;

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
	 * Derived from (0018,0091) Echo train length (Number of lines in k- space
	 * acquired per excitation per image) and from the (0018,9032) Geometry
	 * category of k-Space traversal.
	 */
	private Integer mrSequenceKSpaceFill;

	/** Corresponding Dicom information : (0018,0024) Sequence name. */
	private String mrSequenceName;
	
	/** (0018, 0020) Scanning sequence Description  */
	@ElementCollection
	private List<Integer> mrScanningSequence;
	
	/** (0018, 0021) Sequence Variant of the scanning sequence */
	@ElementCollection
	private List<Integer> mrSequenceVariant;

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

	public List<MrScanningSequence> getMrScanningSequence() {
		List<MrScanningSequence> mrScanningSequenceList = new ArrayList<MrScanningSequence>();
		if (mrScanningSequence != null) {
			for (Integer mrScanningSequenceId : mrScanningSequence) {
				mrScanningSequenceList.add(MrScanningSequence.getScanningSequence(mrScanningSequenceId));
			}
		}
		return mrScanningSequenceList;
	}

	public void setMrScanningSequence(List<String> mrScanningSequenceList) {
		if (mrScanningSequenceList != null && mrScanningSequenceList.size() > 0) {
			mrScanningSequence = new ArrayList<Integer>();
			for (String scanningSequence : mrScanningSequenceList) {
				mrScanningSequence.add(MrScanningSequence.getIdByType(scanningSequence).getId());
			}
		}
	}

	
	public List<MrSequenceVariant> getMrSequenceVariant() {
		List<MrSequenceVariant> mrSequenceVariantList = new ArrayList<MrSequenceVariant>();
		if (mrSequenceVariant != null) {
			for (Integer mrScanningSequenceId : mrSequenceVariant) {
				mrSequenceVariantList.add(MrSequenceVariant.getSequenceVariant(mrScanningSequenceId));
			}
		}
		return mrSequenceVariantList;
	}

	public void setMrSequenceVariant(List<String> mrSequenceVariantList) {
		if (mrSequenceVariantList != null && mrSequenceVariantList.size() > 0) {
			mrSequenceVariant = new ArrayList<Integer>();
			for (String sequenceVariant : mrSequenceVariantList) {
				mrSequenceVariant.add(MrSequenceVariant.getIdByType(sequenceVariant).getId());
			}
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
