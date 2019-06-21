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
 * MR sequence k-space fill.
 * 
 * @author msimon
 *
 */
public enum MrSequenceKSpaceFill {

	// Conventional Cartesian sequence
	CONVENTIONAL_CARTESIAN_SEQUENCE(1),

	// Non-Conventional sequence
	NON_CONVENTIONAL_SEQUENCE(2),

	// Non-Conventional Cartesian sequence
	NON_CONVENTIONAL_CARTESIAN_SEQUENCE(3),

	// Non-Conventional Non-Cartesian sequence
	NON_CONVENTIONAL_NON_CARTESIAN_SEQUENCE(4);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrSequenceKSpaceFill(final int id) {
		this.id = id;
	}

	/**
	 * Get an MR sequence k-space fill by its id.
	 * 
	 * @param id
	 *            k-space fill id.
	 * @return MR sequence k-space fill.
	 */
	public static MrSequenceKSpaceFill getKSpaceFill(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrSequenceKSpaceFill kSpaceFill : MrSequenceKSpaceFill.values()) {
			if (id.equals(kSpaceFill.getId())) {
				return kSpaceFill;
			}
		}
		throw new IllegalArgumentException("No matching MR sequence k-space fill for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
