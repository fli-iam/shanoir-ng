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

package org.shanoir.ng.manufacturermodel.model;

/**
 * Dataset modality type.
 * 
 * @author msimon
 *
 */
public enum DatasetModalityType {

	/**
	 * MR dataset.
	 */
	MR_DATASET(1),

	/**
	 * MEG Dataset.
	 */
	MEG_DATASET(2),

	/**
	 * CT Dataset.
	 */
	CT_DATASET(3),

	/**
	 * SPECT Dataset.
	 */
	SPECT_DATASET(4),

	/**
	 * PET Dataset
	 */
	PET_DATASET(5),

	/**
	 * EEG Dataset
	 */
	EEG_DATASET(6);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private DatasetModalityType(final int id) {
		this.id = id;
	}

	/**
	 * Get a dataset modality type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return dataset modality type.
	 */
	public static DatasetModalityType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (DatasetModalityType type : DatasetModalityType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching dataset modality type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
