package org.shanoir.ng.examination.dto;

import org.shanoir.ng.shared.core.model.IdName;

public class StudySubjectCenterNamesDTO {

	private IdName study;
	private IdName subject;
	private IdName center;

	/**
	 * @return the study
	 */
	public IdName getStudy() {
		return study;
	}

	/**
	 * @param study
	 *            the study to set
	 */
	public void setStudy(IdName study) {
		this.study = study;
	}

	/**
	 * @return the subject
	 */
	public IdName getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(IdName subject) {
		this.subject = subject;
	}

	/**
	 * @return the center
	 */
	public IdName getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter(IdName center) {
		this.center = center;
	}

}
