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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/model/mr/ImagedNucleus.java
package org.shanoir.ng.datasetacquisition.model.mr;
=======
package org.shanoir.ng.datasetacquisition.mr;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/mr/ImagedNucleus.java

/**
 * Imaged nucleus.
 * 
 * @author msimon
 *
 */
public enum ImagedNucleus {

	// 1H
	H1(1),

	// 31P
	P31(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ImagedNucleus(final int id) {
		this.id = id;
	}

	/**
	 * Get an imaged nucleus by its id.
	 * 
	 * @param id
	 *            imaged nucleus id.
	 * @return imaged nucleus.
	 */
	public static ImagedNucleus getNucleus(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ImagedNucleus nucleus : ImagedNucleus.values()) {
			if (id.equals(nucleus.getId())) {
				return nucleus;
			}
		}
		throw new IllegalArgumentException("No matching imaged nucleus for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
}
