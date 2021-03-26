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

/**
 * 
 */
package org.shanoir.ng.subject.dto;

import org.shanoir.ng.subjectstudy.dto.SubjectStudyDTO;

/**
 * Simple DTO for Subject.
 * This class is used as response for findSubjectsByStudyId request.
 * @author yyao
 *
 */
public class SimpleSubjectDTO {
	
	private Long id;
    
    private String name;
    
    private String identifier;
    
    private SubjectStudyDTO subjectStudy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public SubjectStudyDTO getSubjectStudy() {
		return subjectStudy;
	}

	public void setSubjectStudy(SubjectStudyDTO subjectStudy) {
		this.subjectStudy = subjectStudy;
	}

}
