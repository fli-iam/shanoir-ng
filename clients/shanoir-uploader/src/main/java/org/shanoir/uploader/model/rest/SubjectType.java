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

package org.shanoir.uploader.model.rest;

/**
 * Subject type.
 * 
 * @author msimon
 *
 */
public enum SubjectType {

	/**
	 * Healthy volunteer.
	 */
	HEALTHY_VOLUNTEER(1, "Healthy volunteer"),

	/**
	 * Patient.
	 */
	PATIENT(2, "Patient"),

	/**
	 * Phantom.
	 */
	PHANTOM(3, "Phantom");

	private int id;
	
	private String name;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private SubjectType(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Get a subject type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return subject type.
	 */
	public static SubjectType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (SubjectType type : SubjectType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching subject type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return this.name;
	}

}
