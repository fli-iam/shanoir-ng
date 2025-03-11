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
package org.shanoir.ng.importer.strategies.protocol;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.datasetacquisition.model.pet.PetProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author yyao
 *
 */
@Component
public class PetProtocolStrategy {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(PetProtocolStrategy.class);
	
	public PetProtocol generateProtocolForSerie(Attributes attributes) {		
		PetProtocol petProtocol = new PetProtocol();  
		
		/** (0028, 0010) Rows */
		final Integer dimensionX = attributes.getInt(Tag.Rows, 0);
		LOG.debug("extractMetadata : dimensionX=" + dimensionX);
		petProtocol.setDimensionX(dimensionX);

		/** (0028, 0011) Columns */
		final Integer dimensionY = attributes.getInt(Tag.Columns, 0);
		LOG.debug("extractMetadata : dimensionY=" + dimensionY);
		petProtocol.setDimensionY(dimensionY);
		
		/** (0054, 0081) Number of Slices */
		final Integer numberOfSlices = attributes.getInt(Tag.NumberOfSlices, 0);
		LOG.debug("extractMetadata : numberOfSlices=" + numberOfSlices);
		petProtocol.setNumberOfSlices(numberOfSlices);
		
		/**
		 * (0028, 0030) Pixel Spacing in X and Y direction in mm. 
		 * The unit of measure of voxel size X, must be in mm.
		 */
		final double[] pixelspacing = attributes.getDoubles(Tag.PixelSpacing);
		if (pixelspacing != null && pixelspacing.length == 2) {
			final Double voxelSizeX = pixelspacing[0];
			final Double voxelSizeY = pixelspacing[1];
			LOG.debug("extractMetadata : pixelSpacingX=" + voxelSizeX);
			LOG.debug("extractMetadata : pixelSpacingY=" + voxelSizeY);
			petProtocol.setVoxelSizeX(voxelSizeX);
			petProtocol.setVoxelSizeY(voxelSizeY);
		}
		
		/**
		 * (0018, 0050) Slice Thickness in mm. 
		 * The unit of measure of voxel size Z, must be in mm.
		 */
		final Double voxelSizeZ = attributes.getDouble(Tag.SliceThickness, 0);
		LOG.debug("extractMetadata : voxelSizeZ=" + voxelSizeZ);
		petProtocol.setVoxelSizeZ(voxelSizeZ);
		
		/** (0054, 1101) Attenuation Correction Method */
		final String attenuationCorrectionMethod = attributes.getString(Tag.AttenuationCorrectionMethod);
		LOG.debug("extractMetadata : attenuationCorrectionMethod=" + attenuationCorrectionMethod);
		petProtocol.setAttenuationCorrectionMethod(attenuationCorrectionMethod);

		/** (0018,1210) Convolution kernel */
		final String convolutionKernel = attributes.getString(Tag.ConvolutionKernel);
		LOG.debug("extractMetadata : convolutionKernel=" + convolutionKernel);
		petProtocol.setConvolutionKernel(convolutionKernel);

		/** (0054, 1102) Decay Correction */
		final String decayCorrection = attributes.getString(Tag.DecayCorrection);
		LOG.debug("extractMetadata : decayCorrection=" + decayCorrection);
		petProtocol.setDecayCorrection(decayCorrection);

		/** (0054, 1321) Decay Factor */
		final Integer decayFactor = attributes.getInt(Tag.DecayFactor, 0);
		LOG.debug("extractMetadata : decayFactor=" + decayFactor);
		petProtocol.setDecayFactor(decayFactor);
		
		/** (0054,1322) Dose calibration factor */
		final Integer doseCalibrationFactor = attributes.getInt(Tag.DoseCalibrationFactor, 0);
		LOG.debug("extractMetadata : doseCalibrationFactor=" + doseCalibrationFactor);
		petProtocol.setDoseCalibrationFactor(doseCalibrationFactor);

		/**
		 * (0054,0014) Energy window lower limit in KeV. 
		 * The unit of measure of the energy window lower limit must be in KeV.
		 */
		final Integer energyWindowLowerLimit = attributes.getInt(Tag.EnergyWindowLowerLimit, 0);
		LOG.debug("extractMetadata : energyWindowLowerLimit=" + energyWindowLowerLimit);
		petProtocol.setEnergyWindowLowerLimit(energyWindowLowerLimit);

		/**
		 * (0054,0015) Energy window upper limit in KeV. 
		 * The unit of measure of the energy window upper limit must be in KeV.
		 */
		final Integer energyWindowUpperLimit = attributes.getInt(Tag.EnergyWindowUpperLimit, 0);
		LOG.debug("extractMetadata : energyWindowUpperLimit=" + energyWindowUpperLimit);
		petProtocol.setEnergyWindowUpperLimit(energyWindowUpperLimit);

		/** (0018,9739) number of iterations */
		final String numberOfIterations = attributes.getString(Tag.NumberOfIterations);
		LOG.debug("extractMetadata : numberOfIterations=" + numberOfIterations);
		petProtocol.setNumberOfIterations(numberOfIterations);
		
		/** (0018,9740) number of subsets */
		final String numberOfSubsets = attributes.getString(Tag.NumberOfSubsets);
		LOG.debug("extractMetadata : numberOfSubsets=" + numberOfSubsets);
		petProtocol.setNumberOfSubsets(numberOfSubsets);
		
		/**
		 * (0018,1075) Radionuclide Half Life in sec. 
		 * The unit of measure of the radionuclide half life must be in sec.
		 */
		final Double radionuclideHalfLife = attributes.getDouble(Tag.RadionuclideHalfLife, 0);
		LOG.debug("extractMetadata : radionuclideHalfLife=" + radionuclideHalfLife);
		petProtocol.setRadionuclideHalfLife(radionuclideHalfLife);

		/**
		 * (0018,1074) Radionuclide Total Dose in bq. 
		 * The unit of measure of the radionuclide total dose must be in bq.
		 */
		final Integer radionuclideTotalDose = attributes.getInt(Tag.RadionuclideTotalDose, 0);
		LOG.debug("extractMetadata : radionuclideTotalDose=" + radionuclideTotalDose);
		petProtocol.setRadionuclideTotalDose(radionuclideTotalDose);

		/** Radiopharmaceutical Code */
		final String radiopharmaceuticalCode = attributes.getString(Tag.Radiopharmaceutical);
		LOG.debug("extractMetadata : radiopharmaceuticalCode=" + radiopharmaceuticalCode);
		petProtocol.setRadiopharmaceuticalCode(radiopharmaceuticalCode);

		/** (0054, 1100) Randoms Correction Method */
		final String randomsCorrectionMethod = attributes.getString(Tag.RandomsCorrectionMethod);
		LOG.debug("extractMetadata : randomsCorrectionMethod=" + randomsCorrectionMethod);
		petProtocol.setRandomsCorrectionMethod(randomsCorrectionMethod);

		/** (0054, 1103) Reconstruction Method */
		final String reconstructionMethod = attributes.getString(Tag.ReconstructionMethod);
		LOG.debug("extractMetadata : reconstructionMethod=" + reconstructionMethod);
		petProtocol.setReconstructionMethod(reconstructionMethod);

		/** (0028, 1053) Rescale Slope */
		final Integer rescaleSlope = attributes.getInt(Tag.RescaleSlope, 0);
		LOG.debug("extractMetadata : rescaleSlope=" + rescaleSlope);
		petProtocol.setRescaleSlope(rescaleSlope);

		/** (0028, 1054) Rescale Type */
		final String rescaleType = attributes.getString(Tag.RescaleType);
		LOG.debug("extractMetadata : rescaleType=" + rescaleType);
		petProtocol.setRescaleType(rescaleType);

		/** (0054, 1105) Scatter Correction Method */
		final String scatterCorrectionMethod = attributes.getString(Tag.ScatterCorrectionMethod);
		LOG.debug("extractMetadata : scatterCorrectionMethod=" + scatterCorrectionMethod);
		petProtocol.setScatterCorrectionMethod(scatterCorrectionMethod);

		/** (0054,1323) Scatter fraction factor */
		final Integer scatterFractionFactor = attributes.getInt(Tag.ScatterFractionFactor, 0);
		LOG.debug("extractMetadata : scatterFractionFactor=" + scatterFractionFactor);
		petProtocol.setScatterFractionFactor(scatterFractionFactor);

		/** (0054, 1001) Units */
		final String units = attributes.getString(Tag.Units);
		LOG.debug("extractMetadata : units=" + units);
		petProtocol.setUnits(units);
		
		return petProtocol;
	}

}
