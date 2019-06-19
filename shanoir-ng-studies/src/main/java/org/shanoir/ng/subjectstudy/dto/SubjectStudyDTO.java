package org.shanoir.ng.subjectstudy.dto;

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
