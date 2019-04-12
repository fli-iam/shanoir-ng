package org.shanoir.ng.datasetacquisition.model.mr;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.model.DiffusionGradient;

/**
 * MR protocol.
 * 
 * @author msimon
 *
 */
@Entity
public class MrProtocol extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -5850582993918561681L;

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
	 * (0018,9089) VR=FD, VM=3 Diffusion Gradient Orientation. Ordered by rank
	 * of arrival.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DiffusionGradient> diffusionGradients;

//	/** (0018,0081) VR=DS, VM=1 Echo Time In millisec. */
//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
//	private List<EchoTime> echoTimes;

	/**
	 * (0018,0091) Number of lines in k-space acquired per excitation per image.
	 */
	private Integer echoTrainLength;

	/** filters : private Siemens field (0051,1016). */
	private String filters;

//	/** (0018,1314) Flip Angle In degrees. */
//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
//	private List<FlipAngle> flipAngles;

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

//	/**
//	 * (0018,0082) Inversion time For IR sequences only. Ordered by rank of
//	 * arrival.
//	 */
//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
//	private List<InversionTime> inversionTimeList;

	/** The MR Dataset acquisition. */
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "mrProtocol")
	private MrDatasetAcquisition mrDatasetAcquisition;

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

	/** Origin metadata. */
	@OneToOne(cascade = CascadeType.ALL)
	private MrProtocolMetadata originMetadata;

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

//	/**
//	 * (0018,0080) Repetition time In millisec. Not available for all seq.
//	 * Ordered by rank of arrival.
//	 */
//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mrProtocol", cascade = CascadeType.ALL, orphanRemoval = true)
//	private List<RepetitionTime> repetitionTimeList;

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

	/** Metadata updated by study card. */
	@OneToOne
	private MrProtocolSCMetadata updatedMetadata;

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
//
//	/**
//	 * @return the echoTimes
//	 */
//	public List<EchoTime> getEchoTimes() {
//		return echoTimes;
//	}
//
//	/**
//	 * @param echoTimes
//	 *            the echoTimes to set
//	 */
//	public void setEchoTimes(List<EchoTime> echoTimes) {
//		this.echoTimes = echoTimes;
//	}

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

//	/**
//	 * @return the flipAngles
//	 */
//	public List<FlipAngle> getFlipAngles() {
//		return flipAngles;
//	}
//
//	/**
//	 * @param flipAngles
//	 *            the flipAngles to set
//	 */
//	public void setFlipAngles(List<FlipAngle> flipAngles) {
//		this.flipAngles = flipAngles;
//	}

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

//	/**
//	 * @return the inversionTimeList
//	 */
//	public List<InversionTime> getInversionTimeList() {
//		return inversionTimeList;
//	}
//
//	/**
//	 * @param inversionTimeList
//	 *            the inversionTimeList to set
//	 */
//	public void setInversionTimeList(List<InversionTime> inversionTimeList) {
//		this.inversionTimeList = inversionTimeList;
//	}

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
	 * @return the originMetadata
	 */
	public MrProtocolMetadata getOriginMetadata() {
		return originMetadata;
	}

	/**
	 * @param originMetadata
	 *            the originMetadata to set
	 */
	public void setOriginMetadata(MrProtocolMetadata originMetadata) {
		this.originMetadata = originMetadata;
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

//	/**
//	 * @return the repetitionTimeList
//	 */
//	public List<RepetitionTime> getRepetitionTimeList() {
//		return repetitionTimeList;
//	}
//
//	/**
//	 * @param repetitionTimeList
//	 *            the repetitionTimeList to set
//	 */
//	public void setRepetitionTimeList(List<RepetitionTime> repetitionTimeList) {
//		this.repetitionTimeList = repetitionTimeList;
//	}

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
	 * @return the updatedMetadata
	 */
	public MrProtocolSCMetadata getUpdatedMetadata() {
		return updatedMetadata;
	}

	/**
	 * @param updatedMetadata
	 *            the updatedMetadata to set
	 */
	public void setUpdatedMetadata(MrProtocolSCMetadata updatedMetadata) {
		this.updatedMetadata = updatedMetadata;
	}

}
