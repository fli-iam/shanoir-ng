package org.shanoir.uploader.model.dto;

public class SubjectStudyDTO {
	
	private Long id;

	private Long StudyId;

	private boolean physicallyInvolved;

	private String subjectStudyIdentifier;

	private String subjectType;
	
	public SubjectStudyDTO(Long id, Long studyId, boolean physicallyInvolved, String subjectStudyIdentifier,
			String subjectType) {
		super();
		this.id = id;
		StudyId = studyId;
		this.physicallyInvolved = physicallyInvolved;
		this.subjectStudyIdentifier = subjectStudyIdentifier;
		this.subjectType = subjectType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isPhysicallyInvolved() {
		return physicallyInvolved;
	}

	public void setPhysicallyInvolved(boolean physicallyInvolved) {
		this.physicallyInvolved = physicallyInvolved;
	}

	public String getSubjectStudyIdentifier() {
		return subjectStudyIdentifier;
	}

	public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
		this.subjectStudyIdentifier = subjectStudyIdentifier;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}

	public Long getStudyId() {
		return StudyId;
	}

	public void setStudyId(Long studyId) {
		StudyId = studyId;
	}

}
