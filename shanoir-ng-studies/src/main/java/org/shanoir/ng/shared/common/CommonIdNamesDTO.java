package org.shanoir.ng.shared.common;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * DTO with center name, study name and subject name.
 * 
 * @author ifakhfakh
 *
 */
public class CommonIdNamesDTO {

	private IdNameDTO center;
	private IdNameDTO study;
	private IdNameDTO subject;

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

}
