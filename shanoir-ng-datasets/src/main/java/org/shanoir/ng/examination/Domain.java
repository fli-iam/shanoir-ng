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

package org.shanoir.ng.examination;

/**
 * Domain.
 * 
 * @author ifakhfakh
 *
 */
public enum Domain {

	/***
	 * ambulation.
	 */
	AMBULATION(1),

	/**
	 * bowel-bladder-functions.
	 */
	BOWEL_BLADDER_FUNCTIONS(2),

	/***
	 * brainstem-functions.
	 */
	BRAINSTEM_FUNCTIONS(3),

	/***
	 * calculation.
	 */
	CALCULATION(4),

	/***
	 * cerebellar-functions.
	 */
	CEREBELLAR_FUNCTIONS(5),

	/***
	 * cerebral-functions.
	 */
	CEREBRAL_FUNCTIONS(6),

	/***
	 * constructive-realization.
	 */
	CONSTRUCTIVE_REALIZATION(7),

	/***
	 * episodic-memory.
	 */
	EPISODIC_MEMORY(8),

	/***
	 * executive functions.
	 */
	EXECUTIVE_FUNCTIONS(9),

	/***
	 * global-cognitive-efficiency.
	 */
	GLOBAL_COGNITIVE_EFFICIENCY(10),

	/***
	 * language.
	 */
	LANGUAGE(11),

	/***
	 * neurological-functions.
	 */
	NEUROLOGICAL_FUNCTIONS(12),

	/***
	 * optic-functions.
	 */
	OPTIC_FUNCTIONS(13),

	/***
	 * pyramidal-functions.
	 */
	PYRAMIDAL_FUNCTIONS(14),

	/***
	 * sensory-functions.
	 */
	SENSORY_FUNCTIONS(15),

	/***
	 * temporo-spatial-orientation.
	 */
	TEMPORO_SPATIAL_ORIENTATION(16);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private Domain(final int id) {
		this.id = id;
	}

	/**
	 * Get a domain by its id.
	 * 
	 * @param id
	 *            domain id.
	 * @return domain.
	 */
	public static Domain getDomain(final Integer id) {
		if (id == null) {
			return null;
		}
		for (Domain domain : Domain.values()) {
			if (id.equals(domain.getId())) {
				return domain;
			}
		}
		throw new IllegalArgumentException("No matching domain for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
