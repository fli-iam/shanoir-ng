package org.shanoir.uploader.model.dto.rest;

import java.util.List;

public class SubjectStudyDTO {
	
	private Long id;

	private IdNameDTO subject;
	
	private IdNameDTO study;

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

	public IdNameDTO getSubject() {
		return subject;
	}

	public void setSubject(IdNameDTO subject) {
		this.subject = subject;
	}

	public IdNameDTO getStudy() {
		return study;
	}

	public void setStudy(IdNameDTO study) {
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
