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

package org.shanoir.ng.examination.model;

/**
 * Quality.
 * 
 * @author ifakhfakh
 *
 */
public enum Quality {

	/***
	 * ambulation performance with bilateral assistance.
	 */
	AMBULATION_PERFORMANCE_WITH_BILATERAL_ASSISTANCE(1),

	/**
	 * ambulation performance with unilateral assistance.
	 */
	AMBULATION_PERFORMANCE_WITH_UNILATERAL_ASSISTANCE(2),

	/***
	 * ambulation performance without assistance.
	 */
	AMBULATION_PERFORMANCE_WITHOUT_ASSISTANCE(3),

	/***
	 * bowel-bladder-functions performance.
	 */
	BOWEL_BLADDER_FUNCTIONS_PERFORMANCE(4),

	/***
	 * brainstem-functions performance.
	 */
	BRAINSTEM_FUNCTIONS_PERFORMANCE(5),

	/***
	 * calculation performance.
	 */
	CALCULATION_PERFORMANCE(6),

	/***
	 * cerebellar-functions performance.
	 */
	CEREBELLAR_FUNCTIONS_PERFORMANCE(7),

	/***
	 * cerebral-functions performance.
	 */
	CEREBRAL_FUNCTIONS_PERFORMANCE(8),

	/***
	 * constructive-realization performance.
	 */
	CONSTRUCTIVE_REALIZATION_PERFORMANCE(9),

	/***
	 * disability-status.
	 */
	DISABILITY_STATUS(10),

	/***
	 * episodic-memory performance.
	 */
	EPISODIC_MEMORY_PERFORMANCE(11),

	/***
	 * global-cognitive-performance.
	 */
	GLOBAL_COGNITIVE_PERFORMANCE(12),

	/***
	 * language performance.
	 */
	LANGUAGE_PERFORMANCE(13),

	/***
	 * optic-functions performance.
	 */
	OPTIC_FUNCTIONS_PERFORMANCE(14),

	/***
	 * pyramidal-functions performance.
	 */
	PYRAMIDAL_FUNCTIONS_PERFORMANCE(15),

	/***
	 * sensory-functions performance.
	 */
	SENSORY_FUNCTIONS_PERFORMANCE(16),

	/***
	 * spatial-orientation-orientation-performance.
	 */
	SPATIAL_ORIENTATION_ORIENTATION_PERFORMANCE(17),

	/***
	 * temporal-orientation-orientation-performance.
	 */
	TEMPORAL_ORIENTATION_ORIENTATION_PERFORMANCE(18),

	/***
	 * temporo-spatial-orientation-performance.
	 */
	TEMPORO_SPATIAL_ORIENTATION_PERFORMANCE(19);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private Quality(final int id) {
		this.id = id;
	}

	/**
	 * Get a quality by its id.
	 * 
	 * @param id
	 *            quality id.
	 * @return quality.
	 */
	public static Quality getQuality(final Integer id) {
		if (id == null) {
			return null;
		}
		for (Quality quality : Quality.values()) {
			if (id.equals(quality.getId())) {
				return quality;
			}
		}
		throw new IllegalArgumentException("No matching quality for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
