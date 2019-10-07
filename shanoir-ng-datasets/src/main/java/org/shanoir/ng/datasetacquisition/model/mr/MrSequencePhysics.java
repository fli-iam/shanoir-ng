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
 * MR sequence physics.
 * 
 * @author msimon
 *
 */
public enum MrSequencePhysics {

	// Single-echo spin-echo sequence
	SINGLE_ECHO_SPIN_ECHO_SEQUENCE(1),

	// Multi-echo spin-echo sequence
	MULTI_ECHO_SPIN_ECHO_SEQUENCE(2),

	// Standard single-echo spin-echo sequence
	STANDARD_SINGLE_ECHO_SPIN_ECHO_SEQUENCE(3),

	// Inversion recovery single-echo spin-echo sequence
	INVERSION_RECOVERY_SINGLE_ECHO_SPIN_ECHO_SEQUENCE(4),

	// Spin echo-echo planar imaging
	SPIN_ECHO_ECHO_PLANAR_IMAGING(5),

	// Single shot spin-echo sequence
	SINGLE_SHOT_SPIN_ECHO_SEQUENCE(6),

	// Segmented spin-echo sequence
	SEGMENTED_SPIN_ECHO_SEQUENCE(7),

	// Standard single shot spin-echo sequence
	STANDARD_SINGLE_SHOT_SPIN_ECHO_SEQUENCE(8),

	// Inversion recovery single shot spin-echo sequence
	INVERSION_RECOVERY_SINGLE_SHOT_SPIN_ECHO_SEQUENCE(9),

	// Standard segmented spin-echo sequence
	STANDARD_SEGMENTED_SPIN_ECHO_SEQUENCE(10),

	// Inversion recovery segmented spin-echo sequence
	INVERSION_RECOVERY_SEGMENTED_SPIN_ECHO_SEQUENCE(11),

	// Single-echo gradient-echo sequence
	SINGLE_ECHO_GRADIENT_ECHO_SEQUENCE(12),

	// Multi-echo gradient-echo sequence
	MULTI_ECHO_GRADIENT_ECHO_SEQUENCE(13),

	// Magnetization prepared GRE
	MAGNETIZATION_PREPARED_GRE(14),

	// Spoiled GRE
	SPOILED_GRE(15),

	// Refocused GRE
	REFOCUSED_GRE(16),

	// Magnetization prepared spoiled GRE
	MAGNETIZATION_PREPARED_SPOILED_GRE(17),

	// Steady state GRE FID
	STEADY_STATE_GRE_FID(18),

	// Steady state GRE SE
	STEADY_STATE_GRE_SE(19),

	// Steady state FID-SE
	STEADY_STATE_FID_SE(20),

	// Single-shot GRE EPI
	SINGLE_SHOT_GRE_EPI(21),

	// Segmented GRE EPI
	SEGMENTED_GRE_EPI(22),

	// Standard segmented GRE EPI
	STANDARD_SEGMENTED_GRE_EPI(23),

	// Magnetization prepared segmented GRE EPI
	MAGNETIZATION_PREPARED_SEGMENTED_GRE_EPI(24),

	// Standard single-shot GRE EPI
	STANDARD_SINGLE_SHOT_GRE_EPI(25),

	// Magnetization prepared single-shot GRE EPI
	MAGNETIZATION_PREPARED_SINGLE_SHOT_GRE_EPI(26),

	// Hybrid gradient-echo and spin-echo sequence
	HYBRID_GRADIENT_ECHO_AND_SPIN_ECHO_SEQUENCE(27);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrSequencePhysics(final int id) {
		this.id = id;
	}

	/**
	 * Get an MR sequence physics by its id.
	 * 
	 * @param id
	 *            physics id.
	 * @return MR sequence physics.
	 */
	public static MrSequencePhysics getPhysics(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrSequencePhysics physics : MrSequencePhysics.values()) {
			if (id.equals(physics.getId())) {
				return physics;
			}
		}
		throw new IllegalArgumentException("No matching MR sequence physics for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
