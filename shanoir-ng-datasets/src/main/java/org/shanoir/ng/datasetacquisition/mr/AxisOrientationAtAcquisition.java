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

package org.shanoir.ng.datasetacquisition.mr;

/**
 * Axis orientation at acquisition.
 * 
 * @author msimon
 *
 */
public enum AxisOrientationAtAcquisition {

	// Bi-callosal line
	BI_CALLOSAL_LINE(1),

	// AC PC line
	AC_PC_LINE(2),

	// Central sulcus line
	CENTRAL_SULCUS_LINE(3),

	// Lateral sulcus line
	LATERAL_SULCUS_LINE(4);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private AxisOrientationAtAcquisition(final int id) {
		this.id = id;
	}

	/**
	 * Get an axis orientation at acquisition by its id.
	 * 
	 * @param id
	 *            axis orientation id.
	 * @return axis orientation at acquisition.
	 */
	public static AxisOrientationAtAcquisition getAxisOrientation(final Integer id) {
		if (id == null) {
			return null;
		}
		for (AxisOrientationAtAcquisition orientation : AxisOrientationAtAcquisition.values()) {
			if (id.equals(orientation.getId())) {
				return orientation;
			}
		}
		throw new IllegalArgumentException("No matching axis orientation at acquisition for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
