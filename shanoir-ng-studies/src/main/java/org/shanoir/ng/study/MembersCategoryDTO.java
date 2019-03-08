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

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.studyuser.StudyUserType;

/**
 * DTO of list of members by right type.
 * 
 * @author msimon
 *
 */
public class MembersCategoryDTO {

	private List<IdNameDTO> members;

	private StudyUserType studyUserType;

	/**
	 * Default constructor.
	 */
	public MembersCategoryDTO() {
	}

	/**
	 * Constructor with category and members.
	 * 
	 * @param studyUserType
	 *            category.
	 * @param members
	 *            members.
	 */
	public MembersCategoryDTO(final StudyUserType studyUserType, final List<IdNameDTO> members) {
		this.studyUserType = studyUserType;
		this.members = members;
	}

	/**
	 * @return the members
	 */
	public List<IdNameDTO> getMembers() {
		return members;
	}

	/**
	 * @param members
	 *            the members to set
	 */
	public void setMembers(List<IdNameDTO> members) {
		this.members = members;
	}

	/**
	 * @return the studyUserType
	 */
	public StudyUserType getStudyUserType() {
		return studyUserType;
	}

	/**
	 * @param studyUserType
	 *            the studyUserType to set
	 */
	public void setStudyUserType(StudyUserType studyUserType) {
		this.studyUserType = studyUserType;
	}

}
