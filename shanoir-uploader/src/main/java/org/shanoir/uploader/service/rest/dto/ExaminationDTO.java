package org.shanoir.uploader.service.rest.dto;

import java.util.Date;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.utils.Util;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ExaminationDTO {

	private Long id;

	private IdName center;

	private String comment;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date examinationDate;

	private String note;

	private IdName study;

	private IdName subject;

	private Double subjectWeight;

	private boolean preclinical;
	
	public ExaminationDTO() {
		super();
	}

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
	public Date getExaminationDate() {
		return examinationDate;
	}

	/**
	 * @param examinationDate
	 *            the examinationDate to set
	 */
	public void setExaminationDate(Date examinationDate) {
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
	
	public String toString() {
		final String examinationDate = ShUpConfig.formatter.format(this.getExaminationDate());
		return examinationDate + ", " + this.getComment() + " (id = " + this.getId() + ")";
	}

}