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

package org.shanoir.ng.study;

/**
 * Study type.
 * 
 * @author msimon
 *
 */
public enum StudyType {

	/**
	 * Clinical.
	 */
	CLINICAL(1),

	/**
	 * Preclinical.
	 */
	PRECLINICAL(2),

	/**
	 * Methodological.
	 */
	METHODOLOGICAL(3);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private StudyType(final int id) {
		this.id = id;
	}

	/**
	 * Get a study type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return study type.
	 */
	public static StudyType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (StudyType type : StudyType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching study type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
