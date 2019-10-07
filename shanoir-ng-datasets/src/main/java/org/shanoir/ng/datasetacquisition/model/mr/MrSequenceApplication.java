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

package org.shanoir.ng.datasetacquisition.model.mr;

/**
 * MR sequence application.
 * 
 * @author msimon
 *
 */
public enum MrSequenceApplication {

	// Calibration
	CALIBRATION(1),

	// Morphometry
	MORPHOMETRY(2),

	// Angiography
	ANGIOGRAPHY(3),

	// Contrast agent angio
	CONTRAST_AGENT_ANGIO(4),

	// Time of flight angio
	TIME_OF_FLIGHT_ANGIO(5),

	// Velocity encoded angio
	VELOCITY_ENCODED_ANGIO(6),

	// Perfusion
	PERFUSION(7),

	// Diffusion
	DIFFUSION(8),

	// BOLD
	BOLD(9),

	// Spectroscopy
	SPECTROSCOPY(10),

	// H1 single voxel spectroscopy
	H1_SINGLE_VOXEL_SPECTROSCOPY(11),

	// H1 chemical shift imaging spectroscopy
	H1_CHEMICAL_SHIFT_IMAGING_SPECTROSCOPY(12);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrSequenceApplication(final int id) {
		this.id = id;
	}

	/**
	 * Get an MR sequence application by its id.
	 * 
	 * @param id
	 *            application id.
	 * @return MR sequence application.
	 */
	public static MrSequenceApplication getApplication(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrSequenceApplication constrastAgent : MrSequenceApplication.values()) {
			if (id.equals(constrastAgent.getId())) {
				return constrastAgent;
			}
		}
		throw new IllegalArgumentException("No matching MR sequence application for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
