package org.shanoir.ng.examination;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.datasetacquisition.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Simple examination DTO with information for subject.
 * 
 * @author msimon
 *
 */
public class SubjectExaminationDTO {

	private Long id;
	
	private Long centerId;

	private String centerName;

	private String comment;

	private LocalDate examinationDate;

	private String note;

	private Long studyId;

	private String studyName;

	private IdNameDTO subject;

	private Double subjectWeight;
	
	private List<ExaminationDatasetAcquisitionDTO> datasetAcquisitions;

	public Long getCenterId() {
		return centerId;
	}

	public void setCenterId(Long centerId) {
		this.centerId = centerId;
	}

	public String getCenterName() {
		return centerName;
	}

	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public IdNameDTO getSubject() {
		return subject;
	}

	public void setSubject(IdNameDTO subject) {
		this.subject = subject;
	}

	public Double getSubjectWeight() {
		return subjectWeight;
	}

	public void setSubjectWeight(Double subjectWeight) {
		this.subjectWeight = subjectWeight;
	}

	public void setExaminationDate(LocalDate examinationDate) {
		this.examinationDate = examinationDate;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	public LocalDate getExaminationDate() {
		return examinationDate;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the datasetAcquisitions
	 */
	public List<ExaminationDatasetAcquisitionDTO> getDatasetAcquisitions() {
		return datasetAcquisitions;
	}

	/**
	 * @param datasetAcquisitions
	 *            the datasetAcquisitions to set
	 */
	public void setDatasetAcquisitions(List<ExaminationDatasetAcquisitionDTO> datasetAcquisitions) {
		this.datasetAcquisitions = datasetAcquisitions;
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

}