package org.shanoir.ng.subjectstudy;

import java.util.List;

import org.shanoir.ng.subject.SubjectType;

/**
 * DTO for subject of a study.
 * 
 * @author msimon
 *
 */
public class SubjectStudyDTO {
	
	private Long id;

	private Long subjectId;
	
	private Long studyId;

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

	/**
	 * @return the subjectId
	 */
	public Long getSubjectId() {
		return subjectId;
	}

	/**
	 * @param subjectId the subjectId to set
	 */
	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
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
