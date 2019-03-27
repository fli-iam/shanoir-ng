package org.shanoir.ng.subject.dto;

import org.shanoir.ng.subject.model.Subject;

public class SubjectStudyCardIdDTO {
	
    private Long studyCardId;
    
    private Subject subject;

    

	public Long getStudyCardId() {
		return studyCardId;
	}

	public void setStudyCardId(Long studyCardId) {
		this.studyCardId = studyCardId;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}
    
    

}
