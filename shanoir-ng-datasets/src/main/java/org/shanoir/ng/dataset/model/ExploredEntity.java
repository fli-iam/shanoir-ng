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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/dataset/model/ExploredEntity.java
package org.shanoir.ng.dataset.model;
=======
package org.shanoir.ng.dataset;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/dataset/ExploredEntity.java

/**
 * Explored entity.
 * 
 * @author msimon
 *
 */
public enum ExploredEntity {

	// Anatomical Dataset
	ANATOMICAL_DATASET(1),

	// Functional Dataset
	FUNCTIONAL_DATASET(2),

	// Hemodynamic Dataset
	HEMODYNAMIC_DATASET(3),

	// Metabolic Dataset
	METABOLIC_DATASET(4),

	// Calibration
	CALIBRATION(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ExploredEntity(final int id) {
		this.id = id;
	}

	/**
	 * Get an explored entity by its id.
	 * 
	 * @param id
	 *            explored entity id.
	 * @return explored entity.
	 */
	public static ExploredEntity getEntity(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ExploredEntity entity : ExploredEntity.values()) {
			if (id.equals(entity.getId())) {
				return entity;
			}
		}
		throw new IllegalArgumentException("No matching explored entity for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
