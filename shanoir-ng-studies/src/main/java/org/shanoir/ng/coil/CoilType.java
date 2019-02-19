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

package org.shanoir.ng.coil;

/**
 * Coil type.
 * 
 * @author msimon
 *
 */
public enum CoilType {

	/**
	 * BODY.
	 */
	BODY(1),

	/**
	 * HEAD.
	 */
	HEAD(2),

	/**
	 * SURFACE.
	 */
	SURFACE(3),

	/**
	 * MULTICOIL.
	 */
	MULTICOIL(4),

	/**
	 * EXTREMITY
	 */
	EXTREMITY(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private CoilType(final int id) {
		this.id = id;
	}

	/**
	 * Get a coil type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return coil type.
	 */
	public static CoilType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (CoilType type : CoilType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching coil type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
