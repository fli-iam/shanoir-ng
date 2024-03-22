package org.shanoir.ng.shared.subjectstudy;

public class SubjectStudyDTO {

	private long id;

	private long studyId;

	private long subjectId;

	private Integer subjectType;

	public SubjectStudyDTO(long id, Long studyId, Long subjectId, Integer subjectType) {
		this.id = id;
		this.studyId = studyId;
		this.subjectId = subjectId;
		this.subjectType = subjectType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getStudyId() {
		return studyId;
	}

	public void setStudyId(long studyId) {
		this.studyId = studyId;
	}

	public long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(long subjectId) {
		this.subjectId = subjectId;
	}

	public Integer getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(Integer subjectType) {
		this.subjectType = subjectType;
	}

}
