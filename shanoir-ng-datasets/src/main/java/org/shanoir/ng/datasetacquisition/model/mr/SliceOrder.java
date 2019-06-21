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

/**
 * Slice order.
 * 
 * @author msimon
 *
 */
public enum SliceOrder {

	// Head First-Prone
	HEAD_FIRST_PRONE(1),

	// Head First-Decubitus Right
	HEAD_FIRST_DECUBITUS_RIGHT(2),

	// Feet First-Decubitus Right
	FEET_FIRST_DECUBITUS_RIGHT(3),

	// Feet First-Prone
	FEET_FIRST_PRONE(4),

	// Head First-Supine
	HEAD_FIRST_SUPINE(5),

	// Head First-Decubitus Left
	HEAD_FIRST_DECUBITUS_LEFT(6),

	// Feet First-Decubitus Left
	FEET_FIRST_DECUBITUS_LEFT(7);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private SliceOrder(final int id) {
		this.id = id;
	}

	/**
	 * Get a slice order by its id.
	 * 
	 * @param id
	 *            slice order id.
	 * @return slice order.
	 */
	public static SliceOrder getOrder(final Integer id) {
		if (id == null) {
			return null;
		}
		for (SliceOrder order : SliceOrder.values()) {
			if (id.equals(order.getId())) {
				return order;
			}
		}
		throw new IllegalArgumentException("No matching slice order for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
