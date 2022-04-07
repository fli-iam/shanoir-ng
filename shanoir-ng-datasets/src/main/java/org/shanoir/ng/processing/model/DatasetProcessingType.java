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

package org.shanoir.ng.processing.model;

/**
 * Dataset processing type.
 * 
 * @author msimon
 *
 */
public enum DatasetProcessingType {

	// Segmentation
	SEGMENTATION(1),

	// Boundary surface Based segmentation
	BOUNDARY_SURFACE_BASED_SEGMENTATION(2),

	// Region based segmentation
	REGION_BASED_SEGMENTATION(3),

	// Boundary surface and region based segmentation
	BOUNDARY_SURFACE_AND_REGION_BASED_SEGMENTATION(4),

	// Brain segmentation
	BRAIN_SEGMENTATION(5),

	// Tissues segmentation
	TISSUES_SEGMENTATION(6),

	// Subcortical segmentation
	SUBCORTICAL_SEGMENTATION(7),

	// Lesion segmentation
	LESION_SEGMENTATION(8),

	// Reconstruction
	RECONSTRUCTION(9),

	// Registration
	REGISTRATION(10),

	// Normalization
	NORMALIZATION(11),

	// Registration with distorsion correction
	REGISTRATION_WITH_DISTORSION_CORRECTION(12),

	// Affine registration
	AFFINE_REGISTRATION(13),

	// Non-affine registration
	NON_AFFINE_REGISTRATION(14),

	// Rigid registration
	RIGID_REGISTRATION(15),

	// Affine non-rigid registration
	AFFINE_NON_RIGID_REGISTRATION(16),

	// Mono modality rigid registration
	MONO_MODALITY_RIGID_REGISTRATION(17),

	// Multi modality rigid registration
	MULTI_MODALITY_RIGID_REGISTRATION(18),

	// Mono modality affine non-rigid registration
	MONO_MODALITY_AFFINE_NON_RIGID_REGISTRATION(19),

	// Multi modality affine non-rigid registration
	MULTI_MODALITY_AFFINE_NON_RIGID_REGISTRATION(20),

	// Mono modality non-affine registration
	MONO_MODALITY_NON_AFFINE_REGISTRATION(21),

	// Multi modality non-affine registration
	MULTI_MODALITY_NON_AFFINE_REGISTRATION(22),

	// Resampling
	RESAMPLING(23),

	// Cropping
	CROPPING(24),

	// Re-orientation
	RE_ORIENTATION(25),

	// Intensity modification
	INTENSITY_MODIFICATION(26),

	// Dataset arithmetical operation
	DATASET_ARITHMETICAL_OPERATION(27),

	// Datasets addition
	DATASETS_ADDITION(28),

	// Datasets substraction
	DATASETS_SUBSTRACTION(29),

	// Datasets multiplication
	DATASETS_MULTIPLICATION(30),

	// Datasets division
	DATASETS_DIVISION(31),

	// Datasets logical operation
	DATASETS_LOGICAL_OPERATION(32),

	// Datasets blending
	DATASETS_BLENDING(33),

	// Mesh generation
	MESH_GENERATION(34),

	// Structured mesh generation
	STRUCTURED_MESH_GENERATION(35),

	// Unstructured mesh generation
	UNSTRUCTURED_MESH_GENERATION(36),

	// Filtering
	FILTERING(37),

	// Thresholding
	THRESHOLDING(38),

	// Convolution
	CONVOLUTION(39),

	// Smoothing
	SMOOTHING(40),

	// High-pass filtering
	HIGH_PASS_FILTERING(41),

	// Low-pass filtering
	LOW_PASS_FILTERING(42),

	// Mathematical mprphology filtering
	MATHEMATICAL_MORPHOLOGY_FILTERING(43),

	// Erosion
	EROSION(44),

	// Dilation
	DILATION(45),

	// Opening
	OPENING(46),

	// Closing
	CLOSING(47),

	// Thinning
	THINNING(48),

	// Thickening
	THICKENING(49),

	// Skeletonizing
	SKELETONIZING(50),

	// Distance transform processing
	DISTANCE_TRANSFORM_PROCESSING(51),

	// Dataset transformation
	DATASET_TRANSFORMATION(52),

	// Fourier transformation
	FOURIER_TRANSFORMATION(53),

	// Wavelet transformation
	WAVELET_TRANSFORMATION(54),

	// Restoration
	RESTORATION(55),

	// Denoising
	DENOISING(56),

	// Bias-correction
	BIAS_CORRECTION(57),

	// Distorsion-correction
	DISTORSION_CORRECTION(58),

	// Statistical processing
	STATISTICAL_PROCESSING(59),

	// Mean calculation
	MEAN_CALCULATION(60),

	// Standard deviation calculation
	STANDARD_DEVIATION_CALCULATION(61),

	// Coefficient of variation calculation
	COEFFICIENT_OF_VARIATION_CALCULATION(62),

	// Calibration model estimation
	CALIBRATION_MODEL_ESTIMATION(63),

	// Calibration model application
	CALIBRATION_MODEL_APPLICATION(64),

	// Quantitative parameter estimation
	QUANTITATIVE_PARAMETER_ESTIMATION(65),

	// Quantitative T1 estimation
	QUANTITATIVE_T1_ESTIMATION(66),

	// Quantitative T2 estimation
	QUANTITATIVE_T2_ESTIMATION(67),

	// Quantitative T2 star estimation
	QUANTITATIVE_T2_STAR_ESTIMATION(68),

	// Absolute proton density estimation
	ABSOLUTE_PROTON_DENSITY_ESTIMATION(69),

	// Fractional anisotropy estimation
	FRACTIONAL_ANISOTROPY_ESTIMATION(70),

	// Relative anisotropy estimation
	RELATIVE_ANISOTROPY_ESTIMATION(71),

	// Diffusion tensor calculation
	DIFFUSION_TENSOR_CALCULATION(72),

	// Mean diffusivity calculation
	MEAN_DIFFUSIVITY_CALCULATION(73),

	// Voxel displacement map calculation
	VOXEL_DISPLACEMENT_MAP_CALCULATION(74),

	// Field map estimation
	FIELD_MAP_ESTIMATION(75),

	// Regional cerebral blood flow estimation
	REGIONAL_CEREBRAL_BLOOD_FLOW_ESTIMATION(76),

	// Regional cerebral blood volume estimation
	REGIONAL_CEREBRAL_BLOOD_VOLUME_ESTIMATION(77),

	// Regional mean transit time calculation
	REGIONAL_MEAN_TRANSIT_TIME_CALCULATION(78),

	// Absolute metabolite concentration estimation
	ABSOLUTE_METABOLITE_CONCENTRATION_ESTIMATION(79),

	// Metabolite concentration ratio estimation
	METABOLITE_CONCENTRATION_RATIO_ESTIMATION(80),

	// Blood oxygen level dependent signal changes estimation
	BLOOD_OXYGEN_LEVEL_DEPENDENT_SIGNAL_CHANGES_ESTIMATION(81),

	// Format conversion
	FORMAT_CONVERSION(82);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private DatasetProcessingType(final int id) {
		this.id = id;
	}

	/**
	 * Get a dataset processing type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return dataset processing type.
	 */
	public static DatasetProcessingType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (DatasetProcessingType type : DatasetProcessingType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching dataset processing type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
