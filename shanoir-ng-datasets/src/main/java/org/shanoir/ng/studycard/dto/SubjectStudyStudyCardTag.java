package org.shanoir.ng.studycard.dto;

import org.shanoir.ng.shared.model.SubjectStudy;

public class SubjectStudyStudyCardTag {

	private SubjectStudy subjectStudy;

	private int type;

	public SubjectStudy getSubjectStudy() {
		return subjectStudy;
	}

	public void setSubjectStudy(SubjectStudy subjectStudy) {
		this.subjectStudy = subjectStudy;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
			this.type = type;
	}
	
}
