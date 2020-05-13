/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.examination.dto;



import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

/**
 * Examination DTO with necessary information for front
 * 
 * @author ifakhfak
 *
 */
public class ExaminationDTO {

	private Long id;

	private IdName center;

	private String comment;

	@LocalDateAnnotations
	private LocalDate examinationDate;

	private String note;

	private IdName study;

	private IdName subject;

	private Double subjectWeight;
	
	private boolean preclinical;

	private List<String> extraDataFilePathList;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(final String comment) {
		this.comment = comment;
	}

	/**
	 * @return the examinationDate
	 */
	public LocalDate getExaminationDate() {
		return examinationDate;
	}

	/**
	 * @param examinationDate
	 *            the examinationDate to set
	 */
	public void setExaminationDate(final LocalDate examinationDate) {
		this.examinationDate = examinationDate;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note
	 *            the note to set
	 */
	public void setNote(final String note) {
		this.note = note;
	}

	public IdName getCenter() {
		return center;
	}

	public void setCenter(final IdName center) {
		this.center = center;
	}

	public IdName getStudy() {
		return study;
	}

	public void setStudy(final IdName study) {
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
	public void setSubject(final IdName subject) {
		this.subject = subject;
	}

	/**
	 * @return the subjectWeight
	 */
	public Double getSubjectWeight() {
		return subjectWeight;
	}

	/**
	 * @param subjectWeight
	 *            the subjectWeight to set
	 */
	public void setSubjectWeight(final Double subjectWeight) {
		this.subjectWeight = subjectWeight;
	}
	
	public boolean isPreclinical() {
		return preclinical;
	}

	public void setPreclinical(final boolean preclinical) {
		this.preclinical = preclinical;
	}

	/**
	 * @return the extraDataFilePathList
	 */
	public List<String> getExtraDataFilePathList() {
		return extraDataFilePathList;
	}

	/**
	 * @param extraDataFilePathList the extraDataFilePathList to set
	 */
	public void setExtraDataFilePathList(final List<String> extraDataFilePathList) {
		this.extraDataFilePathList = extraDataFilePathList;
	}

}