package org.shanoir.ng.examination;

import java.util.Date;
import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Examination DTO with necessary information for front
 * 
 * @author ifakhfak
 *
 */
public class ExaminationDTO {
	
	private Long id;
	
	private IdNameDTO subject;
	
	private Date examinationDate;
	
	private IdNameDTO study;
	
	private IdNameDTO center;
	
	private String comment;
	
	private String note;
	
	private Double subjectWeight;
	
	private List<InstrumentBasedAssessment> instrumentBasedAssessmentList;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Date getExaminationDate() {
		return examinationDate;
	}

	public void setExaminationDate(Date examinationDate) {
		this.examinationDate = examinationDate;
	}

	/**
	 * @return the subject
	 */
	public IdNameDTO getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(IdNameDTO subject) {
		this.subject = subject;
	}

	/**
	 * @return the study
	 */
	public IdNameDTO getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(IdNameDTO study) {
		this.study = study;
	}

	/**
	 * @return the center
	 */
	public IdNameDTO getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(IdNameDTO center) {
		this.center = center;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note the note to set
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
	 * @param subjectWeight the subjectWeight to set
	 */
	public void setSubjectWeight(Double subjectWeight) {
		this.subjectWeight = subjectWeight;
	}

	/**
	 * @return the instrumentBasedAssessmentList
	 */
	public List<InstrumentBasedAssessment> getInstrumentBasedAssessmentList() {
		return instrumentBasedAssessmentList;
	}

	/**
	 * @param instrumentBasedAssessmentList the instrumentBasedAssessmentList to set
	 */
	public void setInstrumentBasedAssessmentList(List<InstrumentBasedAssessment> instrumentBasedAssessmentList) {
		this.instrumentBasedAssessmentList = instrumentBasedAssessmentList;
	}
	
	

}
