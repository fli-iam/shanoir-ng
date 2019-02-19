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

/**
 * Parameter quantification dataset nature.
 * 
 * @author msimon
 *
 */
public enum ParameterQuantificationDatasetNature {

	// Quantitative T1 dataset
	QUANTITATIVE_T1_DATASET(1),

	// Quantitative T2 dataset
	QUANTITATIVE_T2_DATASET(2),

	// Quantitative T2 star Dataset
	QUANTITATIVE_T2_STAR_DATASET(3),

	// BOLD dataset
	BOLD_DATASET(4),

	// Fractional anisotropy dataset
	FRACTIONAL_ANISOTROPY_DATASET(5),

	// Mean diffusivity dataset
	MEAN_DIFFUSIVITY_DATASET(6),

	// Relative anistropy dataset
	RELATIVE_ANISOTROPY_DATASET(7),

	// Diffusion tensor dataset
	DIFFUSION_TENSOR_DATASET(8),

	// Absolute protondensity dataset
	ABSOLUTE_PROTONDENSITY_DATASET(9),

	// Absolute metabolite concentration dataset
	ABSOLUTE_METABOLITE_CONCENTRATION_DATASET(10),

	// Metabolite concentration ratio dataset
	METABOLITE_CONCENTRATION_RATIO_DATASET(11),

	// Regional cerebral blood flow dataset
	REGIONAL_CEREBRAL_BLOOD_FLOW_DATASET(12),

	// Regional mean transit time dataset
	REGIONAL_MEAN_TRANSIT_TIME_DATASET(13),

	// Regional cerebral blood volume dataset
	REGIONAL_CEREBRAL_BLOOD_VOLUME_DATASET(14),

	// Magnetization transfer ratio Dataset
	MAGNETIZATION_TRANSFER_RATIO_DATASET(15);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ParameterQuantificationDatasetNature(final int id) {
		this.id = id;
	}

	/**
	 * Get a parameter quantification dataset nature by its id.
	 * 
	 * @param id
	 *            parameter id.
	 * @return parameter quantification dataset nature.
	 */
	public static ParameterQuantificationDatasetNature getNature(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ParameterQuantificationDatasetNature nature : ParameterQuantificationDatasetNature.values()) {
			if (id.equals(nature.getId())) {
				return nature;
			}
		}
		throw new IllegalArgumentException("No matching parameter quantification dataset nature for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
