package org.shanoir.ng.importer.model;

import org.shanoir.ng.shared.core.model.IdName;

public class SubjectStudy {

	private Subject subject;
	
	private IdName study;

	public SubjectStudy(Subject subject, IdName study) {
		this.subject = subject;
		this.study = study;
	}

	/**
	 * @return the subject
	 */
	public Subject getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	/**
	 * @return the study
	 */
	public IdName getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(IdName study) {
		this.study = study;
	}

	
}
