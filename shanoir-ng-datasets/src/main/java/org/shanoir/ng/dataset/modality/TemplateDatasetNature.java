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
 * Template dataset nature.
 * 
 * @author msimon
 *
 */
public enum TemplateDatasetNature {

	// T1 weighted MR template dataset
	T1_WEIGHTED_MR_TEMPLATE_DATASET(1),

	// T2 weighted MR template dataset
	T2_WEIGHTED_MR_TEMPLATE_DATASET(2),

	// Proton density weighted MR template dataset
	PROTON_DENSITY_WEIGHTED_MR_TEMPLATE_DATASET(3);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private TemplateDatasetNature(final int id) {
		this.id = id;
	}

	/**
	 * Get a template dataset nature by its id.
	 * 
	 * @param id
	 *            nature id.
	 * @return template dataset nature.
	 */
	public static TemplateDatasetNature getNature(final Integer id) {
		if (id == null) {
			return null;
		}
		for (TemplateDatasetNature nature : TemplateDatasetNature.values()) {
			if (id.equals(nature.getId())) {
				return nature;
			}
		}
		throw new IllegalArgumentException("No matching template dataset nature for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
