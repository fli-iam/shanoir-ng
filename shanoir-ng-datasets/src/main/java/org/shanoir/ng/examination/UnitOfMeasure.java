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

package org.shanoir.ng.examination;

/**
 * Units of measure.
 * 
 * @author ifakhfakh
 *
 */
public enum UnitOfMeasure {

	MS(1),

	PERCENT(2),

	DEGREES(3),

	G(4),

	GY(5),

	HZ_PX(6),

	KG(7),

	M(8),

	MG(9),

	MG_ML(10),

	MHZ(11),

	ML(12),

	MM(13),

	PX(14);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private UnitOfMeasure(final int id) {
		this.id = id;
	}

	/**
	 * Get a unit Of measure by its id.
	 * 
	 * @param id
	 *            unit Of measure id.
	 * @return unit Of measure.
	 */
	public static UnitOfMeasure getUnit(final Integer id) {
		if (id == null) {
			return null;
		}
		for (UnitOfMeasure measure : UnitOfMeasure.values()) {
			if (id.equals(measure.getId())) {
				return measure;
			}
		}
		throw new IllegalArgumentException("No matching unit Of measure for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
