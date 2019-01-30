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
 * MR dataset nature.
 * 
 * @author msimon
 *
 */
public enum MrDatasetNature {

	// T1 weighted MR dataset
	T1_WEIGHTED_MR_DATASET(1),

	// T2 weighted MR dataset
	T2_WEIGHTED_MR_DATASET(2),

	// T2 star weighted MR dataset
	T2_STAR_WEIGHTED_MR_DATASET(3),

	// Proton density weighted MR dataset
	PROTON_DENSITY_WEIGHTED_MR_DATASET(4),

	// Diffusion weighted MR dataset
	DIFFUSION_WEIGHTED_MR_DATASET(5),

	// Velocity encoded angio MR dataset
	VELOCITY_ENCODED_ANGIO_MR_DATASET(6),

	// Time of flight MR dataset
	TIME_OF_FLIGHT_MR_DATASET(7),

	// Contrast agent used angio MR dataset
	CONTRAST_AGENT_USED_ANGIO_MR_DATASET(8),

	// Spin tagging perfusion MR dataset
	SPIN_TAGGING_PERFUSION_MR_DATASET(9),

	// T1 weighted DCE MR dataset
	T1_WEIGHTED_DCE_MR_DATASET(10),

	// T2 weighted DCE MR dataset
	T2_WEIGHTED_DCE_MR_DATASET(11),

	// T2 star weighted DCE MR dataset
	T2_STAR_WEIGHTED_DCE_MR_DATASET(12),

	// Field map dataset short echo time
	FIELD_MAP_DATASET_SHORT_ECHO_TIME(13),

	// Field map dataset long echo time
	FIELD_MAP_DATASET_LONG_ECHO_TIME(14),

	// H1 single-voxel spectroscopy dataset
	H1_SINGLE_VOXEL_SPECTROSCOPY_DATASET(15),

	// H1 spectroscopic imaging dataset
	H1_SPECTROSCOPIC_IMAGING_DATASET(16);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrDatasetNature(final int id) {
		this.id = id;
	}

	/**
	 * Get an MR dataset nature by its id.
	 * 
	 * @param id
	 *            MR dataset nature id.
	 * @return MR dataset nature.
	 */
	public static MrDatasetNature getNature(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrDatasetNature nature : MrDatasetNature.values()) {
			if (id.equals(nature.getId())) {
				return nature;
			}
		}
		throw new IllegalArgumentException("No matching MR dataset nature for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
