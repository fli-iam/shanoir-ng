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
package org.shanoir.uploader.service.rest.dto;

/**
 * Sex.
 * 
 * @author msimon
 *
 */
public enum Sex {

	/**
	 * Male.
	 */
	M(1),

	/**
	 * Female.
	 */
	F(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private Sex(final int id) {
		this.id = id;
	}

	/**
	 * Get a sex by its id.
	 * 
	 * @param id
	 *            sex id.
	 * @return sex.
	 */
	public static Sex getSex(final Integer id) {
		if (id == null) {
			return null;
		}
		for (Sex sex : Sex.values()) {
			if (id.equals(sex.getId())) {
				return sex;
			}
		}
		throw new IllegalArgumentException("No matching sex for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
