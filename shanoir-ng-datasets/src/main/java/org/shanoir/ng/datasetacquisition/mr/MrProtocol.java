package org.shanoir.ng.datasetacquisition.mr;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.shanoir.ng.shared.model.DiffusionGradient;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;

/**
 * MR protocol.
 * 
 * @author msimon
 *
 */
@Entity
public class MrProtocol extends AbstractGenericItem {

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
	 * (0018,9073) Acquisition duration In millisec. The unit of measure of the
	 * acquisition duration must be in millisec.
	 */
	private Double acquisitionDuration;

	/**
	 * Extracted from (0018,1310) Acquisition matrix Dimensions of the acquired
	 * frequency /phase data before reconstruction. Multi-valued: frequency
	 * rows\frequency columns\phase rows\phase columns. The unit of measure of
	 * the acquisition resolution X must be in px.
	 */
	private Integer acquisitionResolutionX;

	/**
	 * Extracted from (0018,1310) Acquisition matrix Dimensions of the acquired
	 * frequency /phase data before reconstruction. Multi-valued: frequency
	 * rows\frequency columns\phase rows\phase columns. The unit of measure of
	 * the acquisition resolution Y must be in px.
	 */
	private Integer acquisitionResolutionY;

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
	 * (0018,9089) VR=FD, VM=3 Diffusion Gradient Orientation. Ordered by rank
	 * of arrival.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DiffusionGradient> diffusionGradients;

	/** (0018,0081) VR=DS, VM=1 Echo Time In millisec. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EchoTime> echoTimes;

	/**
	 * (0018,0091) Number of lines in k-space acquired per excitation per image.
	 */
	private Integer echoTrainLength;

	/** filters : private Siemens field (0051,1016). */
	private String filters;

	/** (0018,1314) Flip Angle In degrees. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FlipAngle> flipAngles;

	/**
	 * Rows (0028,0010) and first value of Pixel Spacing (0028,0030). The unit
	 * of measure of the field of view X must be in mm.
	 */
	private Double fovX;

	/**
	 * Rows (0028,0010) and second value of Pixel Spacing (0028,0030). The unit
	 * of measure of the field of view Y must be in mm.
	 */
	private Double fovY;

	/** (0018,0085) Imaged nucleus. */
	private Integer imagedNucleus;

	/**
	 * (0018,0084) Precession frequency in MHz of the nucleus being addressed.
	 * The unit of measure of the imaging frequency must be in Mhz.
	 */
	private Double imagingFrequency;

	/**
	 * Volume : volume injected in milliliters of diluted contrast agent. The
	 * unit of measure of injected volume must be in ml.
	 */
	private Double injectedVolume;

	/**
	 * (0018,0082) Inversion time For IR sequences only. Ordered by rank of
	 * arrival.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InversionTime> inversionTimeList;

	/**
	 * From (0018,0021) Sequence variant where you can find MTC (standing for
	 * Magnetization Transfer Contrast).
	 */
	private Boolean magnetizationTransfer;

	/** The MR Dataset acquisition. */
	@OneToOne(cascade = CascadeType.ALL)
	private MrDatasetAcquisition mrDatasetAcquisition;

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

	/**
	 * (0018,0083) Number of averages Number of times a given pulse sequence is
	 * repeated before any parameter is changed.
	 */
	private Integer numberOfAverages;

	/**
	 * (0018,0089) Number of Phase Encoding Steps Total number of lines in
	 * k-space in the 'y' direction collected during acquisition.
	 */
	private Integer numberOfPhaseEncodingSteps;

	/** Functional only. (0020,0105) Number of Temporal Positions. */
	private Integer numberOfTemporalPositions;

	/** From (0018,1041) Parallel acquisition Contrast/Bolus. */
	private Boolean parallelAcquisition;

	/**
	 * From dicom tag (0018,9078) VR=CS, VM=1 Parallel Acquisition Technique.
	 */
	private Integer parallelAcquisitionTechnique;

	/** From (0018,5100) Patient position. */
	private Integer patientPosition;

	/**
	 * (0018,0094) Percent phase field of view Ratio of field of view dimension
	 * in phase direction to field of view dimension in frequency direction,
	 * expressed as a percent. The unit of measure of the percent phase FOV,
	 * must be in percent.
	 */
	private Double percentPhaseFov;

	/**
	 * (0018,0093) Percent sampling Fraction of acquisition matrix lines
	 * acquired, expressed as a percent. The unit of measure of the percent
	 * sampling must be in percent.
	 */
	private Double percentSampling;

	/**
	 * (0018,0095) Pixel bandwidth Def DICOM : Reciprocal of the total sampling
	 * period, in hertz per pixel. The unit of measure of the pixel bandwidth,
	 * must be in Hz/px.
	 */
	private Double pixelBandwidth;

	/**
	 * (0028,0030) Pixel Spacing Spatial resolution X = first value Spatial
	 * resolution at acquisition X = second value Physical distance in the
	 * patient between the center of each pixel, specified by a numeric pair -
	 * adjacent row spacing (delimiter) adjacent column spacing in mm. The unit
	 * of measure of the pixel spacing spacial resolution X must be in px.
	 */
	private Double pixelSpacingX;

	/**
	 * (0028,0030) Pixel Spacing Spatial resolution X = first value Spatial
	 * resolution at acquisition Y = second value Physical distance in the
	 * patient between the center of each pixel, specified by a numeric pair -
	 * adjacent row spacing (delimiter) adjacent column spacing in mm. The unit
	 * of measure of the pixel spacing spacial resolution Y must be in px.
	 */
	private Double pixelSpacingY;

	/** Corresponding Dicom information : (0018,1030) Protocol name. */
	private String protocolName;

	/** Receiving coil. */
	private Long receivingCoilId;

	/**
	 * (0018,0080) Repetition time In millisec. Not available for all seq.
	 * Ordered by rank of arrival.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RepetitionTime> repetitionTimeList;

	/** From (0018, 9151) Frame reference Date time. */
	private Integer sliceOrder;

	/** Cannot be easily found from DICOM data elements. */
	private Integer sliceOrientationAtAcquisition;

	/**
	 * (0018,0088) Spacing between slices Value of the prescribed spacing to be
	 * applied between the slices in a volume that is to be acquired. The
	 * spacing in mm is defined as the center-to-center distance of adjacent
	 * slices. In mm. The unit of measure of the slice spacing must be in mm.
	 */
	private Double sliceSpacing;

	/**
	 * (0018,0050) Slice thickness Nominal reconstructed slice thickness, in mm.
	 * The unit of measure of the slice thickness must be in mm.
	 */
	private Double sliceThickness;

	/**
	 * Functional only. From (0020,0110) Time delta between Images in a dynamic
	 * or functional set of Images. The unit of measure of the temporal
	 * resolution must be in millisec.
	 */
	private Double temporalResolution;

	/**
	 * From dicom tag (0018,9069) VR=FD, VM=1 Parallel Reduction Factor
	 * In-plane.
	 */
	private Double timeReductionFactorForTheInplaneDirection;

	/**
	 * From dicom tag (0018,9155) VR=FD, VM=1 Parallel Reduction Factor
	 * out-of-plane.
	 */
	private Double timeReductionFactorForTheOutOfplaneDirection;

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
	 * @return the acquisitionDuration
	 */
	public Double getAcquisitionDuration() {
		return acquisitionDuration;
	}

	/**
	 * @param acquisitionDuration
	 *            the acquisitionDuration to set
	 */
	public void setAcquisitionDuration(Double acquisitionDuration) {
		this.acquisitionDuration = acquisitionDuration;
	}

	/**
	 * @return the acquisitionResolutionX
	 */
	public Integer getAcquisitionResolutionX() {
		return acquisitionResolutionX;
	}

	/**
	 * @param acquisitionResolutionX
	 *            the acquisitionResolutionX to set
	 */
	public void setAcquisitionResolutionX(Integer acquisitionResolutionX) {
		this.acquisitionResolutionX = acquisitionResolutionX;
	}

	/**
	 * @return the acquisitionResolutionY
	 */
	public Integer getAcquisitionResolutionY() {
		return acquisitionResolutionY;
	}

	/**
	 * @param acquisitionResolutionY
	 *            the acquisitionResolutionY to set
	 */
	public void setAcquisitionResolutionY(Integer acquisitionResolutionY) {
		this.acquisitionResolutionY = acquisitionResolutionY;
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
	 * @return the echoTimes
	 */
	public List<EchoTime> getEchoTimes() {
		return echoTimes;
	}

	/**
	 * @param echoTimes
	 *            the echoTimes to set
	 */
	public void setEchoTimes(List<EchoTime> echoTimes) {
		this.echoTimes = echoTimes;
	}

	/**
	 * @return the echoTrainLength
	 */
	public Integer getEchoTrainLength() {
		return echoTrainLength;
	}

	/**
	 * @param echoTrainLength
	 *            the echoTrainLength to set
	 */
	public void setEchoTrainLength(Integer echoTrainLength) {
		this.echoTrainLength = echoTrainLength;
	}

	/**
	 * @return the filters
	 */
	public String getFilters() {
		return filters;
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(String filters) {
		this.filters = filters;
	}

	/**
	 * @return the flipAngles
	 */
	public List<FlipAngle> getFlipAngles() {
		return flipAngles;
	}

	/**
	 * @param flipAngles
	 *            the flipAngles to set
	 */
	public void setFlipAngles(List<FlipAngle> flipAngles) {
		this.flipAngles = flipAngles;
	}

	/**
	 * @return the fovX
	 */
	public Double getFovX() {
		return fovX;
	}

	/**
	 * @param fovX
	 *            the fovX to set
	 */
	public void setFovX(Double fovX) {
		this.fovX = fovX;
	}

	/**
	 * @return the fovY
	 */
	public Double getFovY() {
		return fovY;
	}

	/**
	 * @param fovY
	 *            the fovY to set
	 */
	public void setFovY(Double fovY) {
		this.fovY = fovY;
	}

	/**
	 * @return the imagedNucleus
	 */
	public ImagedNucleus getImagedNucleus() {
		return ImagedNucleus.getNucleus(imagedNucleus);
	}

	/**
	 * @param imagedNucleus
	 *            the imagedNucleus to set
	 */
	public void setImagedNucleus(ImagedNucleus imagedNucleus) {
		if (imagedNucleus == null) {
			this.imagedNucleus = null;
		} else {
			this.imagedNucleus = imagedNucleus.getId();
		}
	}

	/**
	 * @return the imagingFrequency
	 */
	public Double getImagingFrequency() {
		return imagingFrequency;
	}

	/**
	 * @param imagingFrequency
	 *            the imagingFrequency to set
	 */
	public void setImagingFrequency(Double imagingFrequency) {
		this.imagingFrequency = imagingFrequency;
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
	 * @return the inversionTimeList
	 */
	public List<InversionTime> getInversionTimeList() {
		return inversionTimeList;
	}

	/**
	 * @param inversionTimeList
	 *            the inversionTimeList to set
	 */
	public void setInversionTimeList(List<InversionTime> inversionTimeList) {
		this.inversionTimeList = inversionTimeList;
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
	 * @return the mrDatasetAcquisition
	 */
	public MrDatasetAcquisition getMrDatasetAcquisition() {
		return mrDatasetAcquisition;
	}

	/**
	 * @param mrDatasetAcquisition
	 *            the mrDatasetAcquisition to set
	 */
	public void setMrDatasetAcquisition(MrDatasetAcquisition mrDatasetAcquisition) {
		this.mrDatasetAcquisition = mrDatasetAcquisition;
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
	 * @return the numberOfAverages
	 */
	public Integer getNumberOfAverages() {
		return numberOfAverages;
	}

	/**
	 * @param numberOfAverages
	 *            the numberOfAverages to set
	 */
	public void setNumberOfAverages(Integer numberOfAverages) {
		this.numberOfAverages = numberOfAverages;
	}

	/**
	 * @return the numberOfPhaseEncodingSteps
	 */
	public Integer getNumberOfPhaseEncodingSteps() {
		return numberOfPhaseEncodingSteps;
	}

	/**
	 * @param numberOfPhaseEncodingSteps
	 *            the numberOfPhaseEncodingSteps to set
	 */
	public void setNumberOfPhaseEncodingSteps(Integer numberOfPhaseEncodingSteps) {
		this.numberOfPhaseEncodingSteps = numberOfPhaseEncodingSteps;
	}

	/**
	 * @return the numberOfTemporalPositions
	 */
	public Integer getNumberOfTemporalPositions() {
		return numberOfTemporalPositions;
	}

	/**
	 * @param numberOfTemporalPositions
	 *            the numberOfTemporalPositions to set
	 */
	public void setNumberOfTemporalPositions(Integer numberOfTemporalPositions) {
		this.numberOfTemporalPositions = numberOfTemporalPositions;
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
	 * @return the patientPosition
	 */
	public PatientPosition getPatientPosition() {
		return PatientPosition.getPosition(patientPosition);
	}

	/**
	 * @param patientPosition
	 *            the patientPosition to set
	 */
	public void setPatientPosition(PatientPosition patientPosition) {
		if (patientPosition == null) {
			this.patientPosition = null;
		} else {
			this.patientPosition = patientPosition.getId();
		}
	}

	/**
	 * @return the percentPhaseFov
	 */
	public Double getPercentPhaseFov() {
		return percentPhaseFov;
	}

	/**
	 * @param percentPhaseFov
	 *            the percentPhaseFov to set
	 */
	public void setPercentPhaseFov(Double percentPhaseFov) {
		this.percentPhaseFov = percentPhaseFov;
	}

	/**
	 * @return the percentSampling
	 */
	public Double getPercentSampling() {
		return percentSampling;
	}

	/**
	 * @param percentSampling
	 *            the percentSampling to set
	 */
	public void setPercentSampling(Double percentSampling) {
		this.percentSampling = percentSampling;
	}

	/**
	 * @return the pixelBandwidth
	 */
	public Double getPixelBandwidth() {
		return pixelBandwidth;
	}

	/**
	 * @param pixelBandwidth
	 *            the pixelBandwidth to set
	 */
	public void setPixelBandwidth(Double pixelBandwidth) {
		this.pixelBandwidth = pixelBandwidth;
	}

	/**
	 * @return the pixelSpacingX
	 */
	public Double getPixelSpacingX() {
		return pixelSpacingX;
	}

	/**
	 * @param pixelSpacingX
	 *            the pixelSpacingX to set
	 */
	public void setPixelSpacingX(Double pixelSpacingX) {
		this.pixelSpacingX = pixelSpacingX;
	}

	/**
	 * @return the pixelSpacingY
	 */
	public Double getPixelSpacingY() {
		return pixelSpacingY;
	}

	/**
	 * @param pixelSpacingY
	 *            the pixelSpacingY to set
	 */
	public void setPixelSpacingY(Double pixelSpacingY) {
		this.pixelSpacingY = pixelSpacingY;
	}

	/**
	 * @return the protocolName
	 */
	public String getProtocolName() {
		return protocolName;
	}

	/**
	 * @param protocolName
	 *            the protocolName to set
	 */
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
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
	 * @return the repetitionTimeList
	 */
	public List<RepetitionTime> getRepetitionTimeList() {
		return repetitionTimeList;
	}

	/**
	 * @param repetitionTimeList
	 *            the repetitionTimeList to set
	 */
	public void setRepetitionTimeList(List<RepetitionTime> repetitionTimeList) {
		this.repetitionTimeList = repetitionTimeList;
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
	 * @return the sliceSpacing
	 */
	public Double getSliceSpacing() {
		return sliceSpacing;
	}

	/**
	 * @param sliceSpacing
	 *            the sliceSpacing to set
	 */
	public void setSliceSpacing(Double sliceSpacing) {
		this.sliceSpacing = sliceSpacing;
	}

	/**
	 * @return the sliceThickness
	 */
	public Double getSliceThickness() {
		return sliceThickness;
	}

	/**
	 * @param sliceThickness
	 *            the sliceThickness to set
	 */
	public void setSliceThickness(Double sliceThickness) {
		this.sliceThickness = sliceThickness;
	}

	/**
	 * @return the temporalResolution
	 */
	public Double getTemporalResolution() {
		return temporalResolution;
	}

	/**
	 * @param temporalResolution
	 *            the temporalResolution to set
	 */
	public void setTemporalResolution(Double temporalResolution) {
		this.temporalResolution = temporalResolution;
	}

	/**
	 * @return the timeReductionFactorForTheInplaneDirection
	 */
	public Double getTimeReductionFactorForTheInplaneDirection() {
		return timeReductionFactorForTheInplaneDirection;
	}

	/**
	 * @param timeReductionFactorForTheInplaneDirection
	 *            the timeReductionFactorForTheInplaneDirection to set
	 */
	public void setTimeReductionFactorForTheInplaneDirection(Double timeReductionFactorForTheInplaneDirection) {
		this.timeReductionFactorForTheInplaneDirection = timeReductionFactorForTheInplaneDirection;
	}

	/**
	 * @return the timeReductionFactorForTheOutOfplaneDirection
	 */
	public Double getTimeReductionFactorForTheOutOfplaneDirection() {
		return timeReductionFactorForTheOutOfplaneDirection;
	}

	/**
	 * @param timeReductionFactorForTheOutOfplaneDirection
	 *            the timeReductionFactorForTheOutOfplaneDirection to set
	 */
	public void setTimeReductionFactorForTheOutOfplaneDirection(Double timeReductionFactorForTheOutOfplaneDirection) {
		this.timeReductionFactorForTheOutOfplaneDirection = timeReductionFactorForTheOutOfplaneDirection;
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
