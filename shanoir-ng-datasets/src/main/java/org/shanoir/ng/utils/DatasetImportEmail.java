package org.shanoir.ng.utils;

import java.util.List;
import java.util.Map;

/**
 * This class represents an EMAIL to be sent after an email was imported.
 * @author JCD
 *
 */
public class DatasetImportEmail {

	List<Long> recipients;

	Map<Long, String> datasets;
	
    String studyName;
    
    String studyId;
    
    String subjectName;

    String examinationId;
    
    Long userId;

    String examDate;

    String studyCard;

	/**
	 * @return the recipients
	 */
	public List<Long> getRecipients() {
		return recipients;
	}

	/**
	 * @param recipients the recipients to set
	 */
	public void setRecipients(List<Long> recipients) {
		this.recipients = recipients;
	}

	/**
	 * @return the datasets
	 */
	public Map<Long, String> getDatasets() {
		return datasets;
	}

	/**
	 * @param datasets the datasets to set
	 */
	public void setDatasets(Map<Long, String>  datasets) {
		this.datasets = datasets;
	}

	/**
	 * @return the studyName
	 */
	public String getStudyName() {
		return studyName;
	}

	/**
	 * @param studyName the studyName to set
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	/**
	 * @return the studyId
	 */
	public String getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the subjectName
	 */
	public String getSubjectName() {
		return subjectName;
	}

	/**
	 * @param subjectName the subjectName to set
	 */
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	/**
	 * @return the examinationId
	 */
	public String getExaminationId() {
		return examinationId;
	}

	/**
	 * @param examinationId the examinationId to set
	 */
	public void setExaminationId(String examinationId) {
		this.examinationId = examinationId;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the examDate
	 */
	public String getExamDate() {
		return examDate;
	}

	/**
	 * @param examDate the examDate to set
	 */
	public void setExamDate(String examDate) {
		this.examDate = examDate;
	}

	/**
	 * @return the studyCard
	 */
	public String getStudyCard() {
		return studyCard;
	}

	/**
	 * @param studyCard the studyCard to set
	 */
	public void setStudyCard(String studyCard) {
		this.studyCard = studyCard;
	}
	
}
