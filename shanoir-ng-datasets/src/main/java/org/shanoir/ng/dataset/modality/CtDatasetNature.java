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

public enum CtDatasetNature {

	NON_CONTRAST_HEAD_CT(1),
	;

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private CtDatasetNature(final int id) {
		this.id = id;
	}

	/**
	 * Get an MR dataset nature by its id.
	 * 
	 * @param id
	 *            MR dataset nature id.
	 * @return MR dataset nature.
	 */
	public static CtDatasetNature getNature(final Integer id) {
		if (id == null) {
			return null;
		}
		for (CtDatasetNature nature : CtDatasetNature.values()) {
			if (id.equals(nature.getId())) {
				return nature;
			}
		}
		throw new IllegalArgumentException("No matching MR dataset nature for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
