package org.shanoir.ng.shared.common;

import org.shanoir.ng.shared.core.model.IdName;

/**
 * DTO with center name, study name and subject name.
 * 
 * @author ifakhfakh
 *
 */
public class CommonIdNamesDTO {

	private IdName center;
	private IdName study;
	private IdName subject;

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

}
