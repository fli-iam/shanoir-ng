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

import org.shanoir.ng.examination.model.InstrumentBasedAssessment;
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

	private List<InstrumentBasedAssessment> instrumentBasedAssessmentList;
	
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
	public void setId(Long id) {
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
	public void setComment(String comment) {
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
	public void setExaminationDate(LocalDate examinationDate) {
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
	public void setNote(String note) {
		this.note = note;
	}

	public IdName getCenter() {
		return center;
	}

	public void setCenter(IdName center) {
		this.center = center;
	}

	public IdName getStudy() {
		return study;
	}

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
	 * @return the subjectWeight
	 */
	public Double getSubjectWeight() {
		return subjectWeight;
	}

	/**
	 * @param subjectWeight
	 *            the subjectWeight to set
	 */
	public void setSubjectWeight(Double subjectWeight) {
		this.subjectWeight = subjectWeight;
	}
	
	public boolean isPreclinical() {
		return preclinical;
	}

	public void setPreclinical(boolean preclinical) {
		this.preclinical = preclinical;
	}

	public List<InstrumentBasedAssessment> getInstrumentBasedAssessmentList() {
		return instrumentBasedAssessmentList;
	}

	public void setInstrumentBasedAssessmentList(List<InstrumentBasedAssessment> instrumentBasedAssessmentList) {
		this.instrumentBasedAssessmentList = instrumentBasedAssessmentList;
	}

}