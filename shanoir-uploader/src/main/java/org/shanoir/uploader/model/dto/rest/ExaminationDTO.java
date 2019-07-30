package org.shanoir.uploader.model.dto.rest;

import java.util.Date;

import org.shanoir.uploader.model.ExportData;
import org.shanoir.uploader.utils.Util;

public class ExaminationDTO {

	private Long id;

	private Long centerId;

	private String centerName;

	private String comment;

	private Date examinationDate;

	private String note;

	private Long studyId;

	private String studyName;

	private IdNameDTO subject;

	private Double subjectWeight;
	
	private boolean preclinical;
	
	public ExaminationDTO(){
	}
	
	public ExaminationDTO(ExportData exportData, Long subjectId) {
		super();
		this.centerId = Long.valueOf(exportData.getStudyCard().getCenter().getId());
		this.centerName = exportData.getStudyCard().getCenter().getName();
		this.comment = exportData.getCommentOfNewExamination();
		this.examinationDate = Util.toDate(exportData.getDateOfNewExamination());
		this.note = null;
		this.studyId = Long.valueOf(exportData.getStudy().getId());
		this.studyName =  exportData.getStudy().getName();
		this.subject = new IdNameDTO(exportData.getSubject().getId(), exportData.getSubject().getName());
		this.subjectWeight = null; 
		this.preclinical = false;
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
	 * @return the centerId
	 */
	public Long getCenterId() {
		return centerId;
	}

	/**
	 * @param centerId
	 *            the centerId to set
	 */
	public void setCenterId(Long centerId) {
		this.centerId = centerId;
	}

	/**
	 * @return the centerName
	 */
	public String getCenterName() {
		return centerName;
	}

	/**
	 * @param centerName
	 *            the centerName to set
	 */
	public void setCenterName(String centerName) {
		this.centerName = centerName;
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
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId
	 *            the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the studyName
	 */
	public String getStudyName() {
		return studyName;
	}

	/**
	 * @param studyName
	 *            the studyName to set
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
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
