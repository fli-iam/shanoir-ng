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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/model/mr/SliceOrientationAtAcquisition.java
package org.shanoir.ng.datasetacquisition.model.mr;
=======
package org.shanoir.ng.datasetacquisition.mr;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/mr/SliceOrientationAtAcquisition.java

/**
 * Slice orientation at acquisition.
 * 
 * @author msimon
 *
 */
public enum SliceOrientationAtAcquisition {

	// Transverse
	TRANSVERSE(1),

	// Coronal
	CORONAL(2),

	// Sagittal
	SAGITTAL(3),

	// Oblique
	OBLIQUE(4);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private SliceOrientationAtAcquisition(final int id) {
		this.id = id;
	}

	/**
	 * Get a slice orientation at acquisition by its id.
	 * 
	 * @param id
	 *            orientation id.
	 * @return slice orientation at acquisition.
	 */
	public static SliceOrientationAtAcquisition getOrientation(final Integer id) {
		if (id == null) {
			return null;
		}
		for (SliceOrientationAtAcquisition orientation : SliceOrientationAtAcquisition.values()) {
			if (id.equals(orientation.getId())) {
				return orientation;
			}
		}
		throw new IllegalArgumentException("No matching slice orientation at acquisition for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
