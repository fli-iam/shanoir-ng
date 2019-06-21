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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/dataset/model/ProcessedDatasetType.java
package org.shanoir.ng.dataset.model;
=======
package org.shanoir.ng.dataset;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/dataset/ProcessedDatasetType.java

/**
 * Processed dataset type.
 * 
 * @author msimon
 *
 */
public enum ProcessedDatasetType {

	// ReconstructedDataset
	RECONSTRUCTEDDATASET(1),

	// NonReconstructedDataset
	NONRECONSTRUCTEDDATASET(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ProcessedDatasetType(final int id) {
		this.id = id;
	}

	/**
	 * Get a processed dataset type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return processed dataset type.
	 */
	public static ProcessedDatasetType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ProcessedDatasetType type : ProcessedDatasetType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching processed dataset type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
