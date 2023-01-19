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

package org.shanoir.ng.subjectstudy.dto;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.tag.model.SubjectTagDTO;

/**
 * DTO for subject of a study.
 * 
 * @author msimon
 *
 */
public class StudyTagDTO extends IdName {
	
	private List<SubjectTagDTO> subjectTag;

	/**
	 * @return the tags
	 */
	public List<SubjectTagDTO> getSubjectTag() {
		return subjectTag;
	}

	/**
	 * @param subjectTag the tags to set
	 */
	public void setTag(List<SubjectTagDTO> subjectTag) {
		this.subjectTag = subjectTag;
	}
}
