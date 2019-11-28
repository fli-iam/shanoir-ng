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
 * Hemispheric dominance.
 * 
 * @author msimon
 *
 */
public enum HemisphericDominance {

	/**
	 * Left.
	 */
	Left(1, "Left"),

	/**
	 * Right.
	 */
	Right(2, "Right");

	private int id;
	
	private String name;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private HemisphericDominance(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Get a hemispheric dominance by its id.
	 * 
	 * @param id
	 *            dominance id.
	 * @return hemispheric dominance.
	 */
	public static HemisphericDominance getDominance(final Integer id) {
		if (id == null) {
			return null;
		}
		for (HemisphericDominance dominance : HemisphericDominance.values()) {
			if (id.equals(dominance.getId())) {
				return dominance;
			}
		}
		throw new IllegalArgumentException("No matching hemispheric dominance for id " + id);
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

}
