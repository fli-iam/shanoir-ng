package org.shanoir.ng.examination.dto;

import org.shanoir.ng.shared.dto.IdNameDTO;

public class StudySubjectCenterNamesDTO {

	private IdNameDTO study;
	private IdNameDTO subject;
	private IdNameDTO center;

	/**
	 * @return the study
	 */
	public IdNameDTO getStudy() {
		return study;
	}

	/**
	 * @param study
	 *            the study to set
	 */
	public void setStudy(IdNameDTO study) {
		this.study = study;
	}

	/**
	 * @return the subject
	 */
	public IdNameDTO getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(IdNameDTO subject) {
		this.subject = subject;
	}

	/**
	 * @return the center
	 */
	public IdNameDTO getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter(IdNameDTO center) {
		this.center = center;
	}

}
