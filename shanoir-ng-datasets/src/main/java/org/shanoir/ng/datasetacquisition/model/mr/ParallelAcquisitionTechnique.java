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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parallel acquisition technique.
 * 
 * @author msimon
 *
 */
public enum ParallelAcquisitionTechnique {

	// PILS
	PILS(1),

	// SENSE
	SENSE(2),

	// SMASH
	SMASH(3),

	// GRAPPA
	GRAPPA(4),
	
	// mSENSE
	M_SENSE(5),
	
	// alternative of mSENSE
	MBSENSE(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ParallelAcquisitionTechnique(final int id) {
		this.id = id;
	}

	private static final Logger LOG = LoggerFactory.getLogger(ParallelAcquisitionTechnique.class);

	/**
	 * Get a parallel acquisition technique by its id.
	 * 
	 * @param id
	 *            technique id.
	 * @return parallel acquisition technique.
	 */
	public static ParallelAcquisitionTechnique getTechnique(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ParallelAcquisitionTechnique technique : ParallelAcquisitionTechnique.values()) {
			if (id.equals(technique.getId())) {
				return technique;
			}
		}
		throw new IllegalArgumentException("No matching parallel acquisition technique for id " + id);
	}

	/**
	 * Get a parallel acquisition technique by its id.
	 * 
	 * @param id
	 *            technique id.
	 * @return parallel acquisition technique.
	 */
	public static ParallelAcquisitionTechnique getIdByTechnique(final String technique) {
		if (technique == null) {
			return null;
		}
		try {
			return ParallelAcquisitionTechnique.valueOf(technique);
		} catch (IllegalArgumentException e) {
			LOG.error("ERROR: Parrallel acquisition technique not found: {}, null was set.", technique);
			// If not found, just return null and log it instead of blocking all import.
			return null;
		}
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
