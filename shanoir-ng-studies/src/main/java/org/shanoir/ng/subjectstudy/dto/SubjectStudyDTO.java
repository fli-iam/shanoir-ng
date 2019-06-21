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

<<<<<<< HEAD:shanoir-ng-studies/src/main/java/org/shanoir/ng/subjectstudy/dto/SubjectStudyDTO.java
package org.shanoir.ng.subjectstudy.dto;
=======
package org.shanoir.ng.subjectstudy;
>>>>>>> upstream/develop:shanoir-ng-studies/src/main/java/org/shanoir/ng/subjectstudy/SubjectStudyDTO.java

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.subject.model.SubjectType;

/**
 * DTO for subject of a study.
 * 
 * @author msimon
 *
 */
public class SubjectStudyDTO {
	
	private Long id;

	private IdName subject;
	
	private IdName study;

	private String subjectStudyIdentifier;

	private SubjectType subjectType;
	
	private boolean physicallyInvolved;
	
	private List<ExaminationDTO> examinationDTO;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IdName getSubject() {
		return subject;
	}

	public void setSubject(IdName subject) {
		this.subject = subject;
	}

	public IdName getStudy() {
		return study;
	}

	public void setStudy(IdName study) {
		this.study = study;
	}

	/**
	 * @return the subjectStudyIdentifier
	 */
	public String getSubjectStudyIdentifier() {
		return subjectStudyIdentifier;
	}

	/**
	 * @param subjectStudyIdentifier the subjectStudyIdentifier to set
	 */
	public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
		this.subjectStudyIdentifier = subjectStudyIdentifier;
	}

	/**
	 * @return the subjectType
	 */
	public SubjectType getSubjectType() {
		return subjectType;
	}

	/**
	 * @param subjectType the subjectType to set
	 */
	public void setSubjectType(SubjectType subjectType) {
		this.subjectType = subjectType;
	}

	/**
	 * @return the physicallyInvolved
	 */
	public boolean isPhysicallyInvolved() {
		return physicallyInvolved;
	}

	/**
	 * @param physicallyInvolved the physicallyInvolved to set
	 */
	public void setPhysicallyInvolved(boolean physicallyInvolved) {
		this.physicallyInvolved = physicallyInvolved;
	}

	/**
	 * @return the examinationDTO
	 */
	public List<ExaminationDTO> getExaminationDTO() {
		return examinationDTO;
	}

	/**
	 * @param examinationDTO the examinationDTO to set
	 */
	public void setExaminationDTO(List<ExaminationDTO> examinationDTO) {
		this.examinationDTO = examinationDTO;
	}
}
