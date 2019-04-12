package org.shanoir.ng.datasetacquisition.model.pet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * PET protocol.
 * 
 * @author msimon
 *
 */
@Entity
public class PetProtocol extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -9089131328921308436L;

	/** (0054, 1101) Attenuation Correction Method */
	private String attenuationCorrectionMethod;

	/** Convolution kernel */
	private String convolutionKernel;

	/** (0054, 1102) Decay Correction */
	private String decayCorrection;

	/** (0054, 1321) Decay Factor */
	private Integer decayFactor;

	/** (0028, 0010) Rows */
	@NotNull
	private Integer dimensionX;

	/** (0028, 0011) Columns */
	@NotNull
	private Integer dimensionY;

	/** Dose calibration factor */
	private Integer doseCalibrationFactor;

	/**
	 * Energy window lower limit in KeV. The unit of measure of the energy
	 * window lower limit must be in KeV.
	 */
	private Integer energyWindowLowerLimit;

	/**
	 * Energy window upper limit in KeV. The unit of measure of the energy
	 * window upper limit must be in KeV.
	 */
	private Integer energyWindowUpperLimit;

	/** number of iterations */
	private String numberOfIterations;

	/** (0054, 0081) Number of Slices */
	@NotNull
	private Integer numberOfSlices;

	/** number of subsets */
	private String numberOfSubsets;

	/** The PET Dataset acquisition. */
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "petProtocol")
	private PetDatasetAcquisition petDatasetAcquisition;

	/**
	 * (0018,1075) Radionuclide Half Life in sec. The unit of measure of the
	 * radionuclide half life must be in sec.
	 */
	private Double radionuclideHalfLife;

	/**
	 * (0018,1074) Radionuclide Total Dose in bq. The unit of measure of the
	 * radionuclide total dose must be in bq.
	 */
	private Integer radionuclideTotalDose;

	/** Radiopharmaceutical Code */
	private String radiopharmaceuticalCode;

	/** (0054, 1100) Randoms Correction Method */
	private String randomsCorrectionMethod;

	/** (0054, 1103) Reconstruction Method */
	private String reconstructionMethod;

	/** (0028, 1053) Rescale Slope */
	private Long rescaleSlope;

	/** (0028, 1054) Rescale Type */
	private String rescaleType;

	/** (0054, 1105) Scatter Correction Method */
	private String scatterCorrectionMethod;

	/** Scatter fraction factor */
	private Integer scatterFractionFactor;

	/** (0054, 1001) Units */
	private String units;

	/**
	 * (0028, 0030) Pixel Spacing in X direction in mm. The unit of measure of
	 * voxel size X, must be in mm.
	 */
	@NotNull
	private String voxelSizeX;

	/**
	 * (0028, 0030) Pixel Spacing in Y direction in mm. The unit of measure of
	 * voxel size Y, must be in mm.
	 */
	@NotNull
	private String voxelSizeY;

	/**
	 * (0018, 0050) Pixel Spacing in YZ direction in mm. The unit of measure of
	 * voxel size Z, must be in mm.
	 */
	@NotNull
	private String voxelSizeZ;

	/**
	 * @return the attenuationCorrectionMethod
	 */
	public String getAttenuationCorrectionMethod() {
		return attenuationCorrectionMethod;
	}

	/**
	 * @param attenuationCorrectionMethod
	 *            the attenuationCorrectionMethod to set
	 */
	public void setAttenuationCorrectionMethod(String attenuationCorrectionMethod) {
		this.attenuationCorrectionMethod = attenuationCorrectionMethod;
	}

	/**
	 * @return the convolutionKernel
	 */
	public String getConvolutionKernel() {
		return convolutionKernel;
	}

	/**
	 * @param convolutionKernel
	 *            the convolutionKernel to set
	 */
	public void setConvolutionKernel(String convolutionKernel) {
		this.convolutionKernel = convolutionKernel;
	}

	/**
	 * @return the decayCorrection
	 */
	public String getDecayCorrection() {
		return decayCorrection;
	}

	/**
	 * @param decayCorrection
	 *            the decayCorrection to set
	 */
	public void setDecayCorrection(String decayCorrection) {
		this.decayCorrection = decayCorrection;
	}

	/**
	 * @return the decayFactor
	 */
	public Integer getDecayFactor() {
		return decayFactor;
	}

	/**
	 * @param decayFactor
	 *            the decayFactor to set
	 */
	public void setDecayFactor(Integer decayFactor) {
		this.decayFactor = decayFactor;
	}

	/**
	 * @return the dimensionX
	 */
	public Integer getDimensionX() {
		return dimensionX;
	}

	/**
	 * @param dimensionX
	 *            the dimensionX to set
	 */
	public void setDimensionX(Integer dimensionX) {
		this.dimensionX = dimensionX;
	}

	/**
	 * @return the dimensionY
	 */
	public Integer getDimensionY() {
		return dimensionY;
	}

	/**
	 * @param dimensionY
	 *            the dimensionY to set
	 */
	public void setDimensionY(Integer dimensionY) {
		this.dimensionY = dimensionY;
	}

	/**
	 * @return the doseCalibrationFactor
	 */
	public Integer getDoseCalibrationFactor() {
		return doseCalibrationFactor;
	}

	/**
	 * @param doseCalibrationFactor
	 *            the doseCalibrationFactor to set
	 */
	public void setDoseCalibrationFactor(Integer doseCalibrationFactor) {
		this.doseCalibrationFactor = doseCalibrationFactor;
	}

	/**
	 * @return the energyWindowLowerLimit
	 */
	public Integer getEnergyWindowLowerLimit() {
		return energyWindowLowerLimit;
	}

	/**
	 * @param energyWindowLowerLimit
	 *            the energyWindowLowerLimit to set
	 */
	public void setEnergyWindowLowerLimit(Integer energyWindowLowerLimit) {
		this.energyWindowLowerLimit = energyWindowLowerLimit;
	}

	/**
	 * @return the energyWindowUpperLimit
	 */
	public Integer getEnergyWindowUpperLimit() {
		return energyWindowUpperLimit;
	}

	/**
	 * @param energyWindowUpperLimit
	 *            the energyWindowUpperLimit to set
	 */
	public void setEnergyWindowUpperLimit(Integer energyWindowUpperLimit) {
		this.energyWindowUpperLimit = energyWindowUpperLimit;
	}

	/**
	 * @return the numberOfIterations
	 */
	public String getNumberOfIterations() {
		return numberOfIterations;
	}

	/**
	 * @param numberOfIterations
	 *            the numberOfIterations to set
	 */
	public void setNumberOfIterations(String numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}

	/**
	 * @return the numberOfSlices
	 */
	public Integer getNumberOfSlices() {
		return numberOfSlices;
	}

	/**
	 * @param numberOfSlices
	 *            the numberOfSlices to set
	 */
	public void setNumberOfSlices(Integer numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}

	/**
	 * @return the numberOfSubsets
	 */
	public String getNumberOfSubsets() {
		return numberOfSubsets;
	}

	/**
	 * @param numberOfSubsets
	 *            the numberOfSubsets to set
	 */
	public void setNumberOfSubsets(String numberOfSubsets) {
		this.numberOfSubsets = numberOfSubsets;
	}

	/**
	 * @return the petDatasetAcquisition
	 */
	public PetDatasetAcquisition getPetDatasetAcquisition() {
		return petDatasetAcquisition;
	}

	/**
	 * @param petDatasetAcquisition
	 *            the petDatasetAcquisition to set
	 */
	public void setPetDatasetAcquisition(PetDatasetAcquisition petDatasetAcquisition) {
		this.petDatasetAcquisition = petDatasetAcquisition;
	}

	/**
	 * @return the radionuclideHalfLife
	 */
	public Double getRadionuclideHalfLife() {
		return radionuclideHalfLife;
	}

	/**
	 * @param radionuclideHalfLife
	 *            the radionuclideHalfLife to set
	 */
	public void setRadionuclideHalfLife(Double radionuclideHalfLife) {
		this.radionuclideHalfLife = radionuclideHalfLife;
	}

	/**
	 * @return the radionuclideTotalDose
	 */
	public Integer getRadionuclideTotalDose() {
		return radionuclideTotalDose;
	}

	/**
	 * @param radionuclideTotalDose
	 *            the radionuclideTotalDose to set
	 */
	public void setRadionuclideTotalDose(Integer radionuclideTotalDose) {
		this.radionuclideTotalDose = radionuclideTotalDose;
	}

	/**
	 * @return the radiopharmaceuticalCode
	 */
	public String getRadiopharmaceuticalCode() {
		return radiopharmaceuticalCode;
	}

	/**
	 * @param radiopharmaceuticalCode
	 *            the radiopharmaceuticalCode to set
	 */
	public void setRadiopharmaceuticalCode(String radiopharmaceuticalCode) {
		this.radiopharmaceuticalCode = radiopharmaceuticalCode;
	}

	/**
	 * @return the randomsCorrectionMethod
	 */
	public String getRandomsCorrectionMethod() {
		return randomsCorrectionMethod;
	}

	/**
	 * @param randomsCorrectionMethod
	 *            the randomsCorrectionMethod to set
	 */
	public void setRandomsCorrectionMethod(String randomsCorrectionMethod) {
		this.randomsCorrectionMethod = randomsCorrectionMethod;
	}

	/**
	 * @return the reconstructionMethod
	 */
	public String getReconstructionMethod() {
		return reconstructionMethod;
	}

	/**
	 * @param reconstructionMethod
	 *            the reconstructionMethod to set
	 */
	public void setReconstructionMethod(String reconstructionMethod) {
		this.reconstructionMethod = reconstructionMethod;
	}

	/**
	 * @return the rescaleSlope
	 */
	public Long getRescaleSlope() {
		return rescaleSlope;
	}

	/**
	 * @param rescaleSlope
	 *            the rescaleSlope to set
	 */
	public void setRescaleSlope(Long rescaleSlope) {
		this.rescaleSlope = rescaleSlope;
	}

	/**
	 * @return the rescaleType
	 */
	public String getRescaleType() {
		return rescaleType;
	}

	/**
	 * @param rescaleType
	 *            the rescaleType to set
	 */
	public void setRescaleType(String rescaleType) {
		this.rescaleType = rescaleType;
	}

	/**
	 * @return the scatterCorrectionMethod
	 */
	public String getScatterCorrectionMethod() {
		return scatterCorrectionMethod;
	}

	/**
	 * @param scatterCorrectionMethod
	 *            the scatterCorrectionMethod to set
	 */
	public void setScatterCorrectionMethod(String scatterCorrectionMethod) {
		this.scatterCorrectionMethod = scatterCorrectionMethod;
	}

	/**
	 * @return the scatterFractionFactor
	 */
	public Integer getScatterFractionFactor() {
		return scatterFractionFactor;
	}

	/**
	 * @param scatterFractionFactor
	 *            the scatterFractionFactor to set
	 */
	public void setScatterFractionFactor(Integer scatterFractionFactor) {
		this.scatterFractionFactor = scatterFractionFactor;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param units
	 *            the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @return the voxelSizeX
	 */
	public String getVoxelSizeX() {
		return voxelSizeX;
	}

	/**
	 * @param voxelSizeX
	 *            the voxelSizeX to set
	 */
	public void setVoxelSizeX(String voxelSizeX) {
		this.voxelSizeX = voxelSizeX;
	}

	/**
	 * @return the voxelSizeY
	 */
	public String getVoxelSizeY() {
		return voxelSizeY;
	}

	/**
	 * @param voxelSizeY
	 *            the voxelSizeY to set
	 */
	public void setVoxelSizeY(String voxelSizeY) {
		this.voxelSizeY = voxelSizeY;
	}

	/**
	 * @return the voxelSizeZ
	 */
	public String getVoxelSizeZ() {
		return voxelSizeZ;
	}

	/**
	 * @param voxelSizeZ
	 *            the voxelSizeZ to set
	 */
	public void setVoxelSizeZ(String voxelSizeZ) {
		this.voxelSizeZ = voxelSizeZ;
	}

}
