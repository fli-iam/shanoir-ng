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

package org.shanoir.ng.study;

/**
 * Study status.
 * 
 * @author msimon
 *
 */
public enum StudyStatus {

	/**
	 * in_progress.
	 */
	IN_PROGRESS(1),

	/**
	 * finished.
	 */
	FINISHED(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private StudyStatus(final int id) {
		this.id = id;
	}

	/**
	 * Get a study status by its id.
	 * 
	 * @param id
	 *            status id.
	 * @return study status.
	 */
	public static StudyStatus getStatus(final Integer id) {
		if (id == null) {
			return null;
		}
		for (StudyStatus status : StudyStatus.values()) {
			if (id.equals(status.getId())) {
				return status;
			}
		}
		throw new IllegalArgumentException("No matching study status for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
