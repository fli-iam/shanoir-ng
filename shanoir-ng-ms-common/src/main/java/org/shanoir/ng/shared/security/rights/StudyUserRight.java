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

package org.shanoir.ng.shared.security.rights;

/**
 * Study rights for an user.
 * 
 * @author msimon, jlouis
 *
 */
public enum StudyUserRight {

	
	/**
	 * The member can edit the study's parameters, the study's members and their rights and protocol files for this study..
	 */
	CAN_ADMINISTRATE(1),
	
	/**
	 *  The member can import data in this study. Must come with CAN_SEE_ALL otherwise the user cannot see the data he has imported.
	 */
	CAN_IMPORT(2),
	
	/**
	 * The member can downlad data from this study.
	 */
	CAN_DOWNLOAD(3),
	
	/**
	 * The member can see all the study's data.
	 */
	CAN_SEE_ALL(4);
	

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private StudyUserRight(final int id) {
		this.id = id;
	}

	/**
	 * Get a study right for an user by its id.
	 * 
	 * @param id
	 *            right id.
	 * @return study right.
	 */
	public static StudyUserRight getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (StudyUserRight type : StudyUserRight.values()) {
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
