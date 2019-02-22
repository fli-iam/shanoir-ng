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

package org.shanoir.ng.studyuser;

/**
 * Study rights for an user.
 * 
 * @author msimon
 *
 */
public enum StudyUserType {

	/**
	 * Is responsible for the research study.
	 */
	RESPONSIBLE(1),

	/**
	 * Can see, download, import datasets and modify the study parameters.
	 */
	SEE_DOWNLOAD_IMPORT_MODIFY(2),

	/**
	 * Can see, download and import datasets.
	 */
	SEE_DOWNLOAD_IMPORT(3),

	/**
	 * Cannot see or download datasets.
	 */
	NOT_SEE_DOWNLOAD(4),

	/**
	 * Can see and download datasets.
	 */
	SEE_DOWNLOAD(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private StudyUserType(final int id) {
		this.id = id;
	}

	/**
	 * Get a study right for an user by its id.
	 * 
	 * @param id
	 *            right id.
	 * @return study right.
	 */
	public static StudyUserType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (StudyUserType type : StudyUserType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching study right for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
