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
 * MR quality procedure type.
 * 
 * @author msimon
 *
 */
public enum MrQualityProcedureType {

	// Magnetic Field Quality Dataset Long Echo Time
	MAGNETIC_FIELD_QUALITY_DATASET_LONG_ECHO_TIME(1),

	// Magnetic Field Quality Dataset Short Echo Time
	MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrQualityProcedureType(final int id) {
		this.id = id;
	}

	/**
	 * Get an MR quality procedure type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return MR quality procedure type.
	 */
	public static MrQualityProcedureType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrQualityProcedureType type : MrQualityProcedureType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching MR quality procedure type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
