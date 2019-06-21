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

package org.shanoir.ng.dataset.model;

/**
 * Cardinality of related subjects.
 * 
 * @author msimon
 *
 */
public enum CardinalityOfRelatedSubjects {

	// Single-Subject Dataset
	SINGLE_SUBJECT_DATASET(1),

	// Multiple-Subjects Dataset
	MULTIPLE_SUBJECTS_DATASET(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private CardinalityOfRelatedSubjects(final int id) {
		this.id = id;
	}

	/**
	 * Get a cardinality by its id.
	 * 
	 * @param id
	 *            cardinality id.
	 * @return cardinality of related subjects.
	 */
	public static CardinalityOfRelatedSubjects getCardinality(final Integer id) {
		if (id == null) {
			return null;
		}
		for (CardinalityOfRelatedSubjects cardinality : CardinalityOfRelatedSubjects.values()) {
			if (id.equals(cardinality.getId())) {
				return cardinality;
			}
		}
		throw new IllegalArgumentException("No matching cardinality of related subjects for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
