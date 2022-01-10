package org.shanoir.uploader.model.rest;

import java.util.Date;

import org.shanoir.uploader.ShUpConfig;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Examination {

	private Long id;

	private Long centerId;

	private String comment;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Europe/Paris")
	private Date examinationDate;

	private String note;

	private Long studyId;

	private Long subjectId;

	private Double subjectWeight;

	private boolean preclinical;
	
	public Examination() {
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

	public Long getCenterId() {
		return centerId;
	}

	public Long getStudyId() {
		return studyId;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setCenterId(Long centerId) {
		this.centerId = centerId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

}