package org.shanoir.ng.examination.dto;



import java.time.LocalDate;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Examination DTO with necessary information for front
 * 
 * @author ifakhfak
 *
 */
public class ExaminationDTO {

	private Long id;

	private IdNameDTO center;

	private String comment;

	@LocalDateAnnotations
	private LocalDate examinationDate;

	private String note;

	private IdNameDTO study;

	private IdNameDTO subject;

	private Double subjectWeight;
	
	private boolean preclinical;
	
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

	public IdNameDTO getCenter() {
		return center;
	}

	public void setCenter(IdNameDTO center) {
		this.center = center;
	}

	public IdNameDTO getStudy() {
		return study;
	}

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

}