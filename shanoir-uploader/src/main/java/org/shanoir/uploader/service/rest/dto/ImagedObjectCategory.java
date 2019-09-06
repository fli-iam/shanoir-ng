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
 * Imaged object category.
 * 
 * @author msimon
 *
 */
public enum ImagedObjectCategory {

	/**
	 * Phantom
	 */
	PHANTOM(1, "Phantom"),

	/**
	 * Living human being
	 */
	LIVING_HUMAN_BEING(2, "Living human being"),

	/**
	 * Human cadaver
	 */
	HUMAN_CADAVER(3, "Human cadaver"),

	/**
	 * Anatomical piece
	 */
	ANATOMICAL_PIECE(4, "Anatomical piece"),

	/**
	 * Animal
	 */
	ANIMAL(5, "Animal");

	private int id;

	private String name;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ImagedObjectCategory(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Get an imaged object category by its id.
	 * 
	 * @param id
	 *            category id.
	 * @return imaged object category.
	 */
	public static ImagedObjectCategory getCategory(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ImagedObjectCategory category : ImagedObjectCategory.values()) {
			if (id.equals(category.getId())) {
				return category;
			}
		}
		throw new IllegalArgumentException("No matching imaged object category for id " + id);
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
