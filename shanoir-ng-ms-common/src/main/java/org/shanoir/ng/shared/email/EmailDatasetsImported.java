package org.shanoir.ng.shared.email;

import java.util.Map;

/**
 * This class represents an EMAIL to be sent after datasets have been imported.
 * 
 * @author JCD, mkain
 *
 */
public class EmailDatasetsImported extends EmailBase {

	Map<Long, String> datasets;
    
    String subjectName;

    String examinationId;
    
    String examDate;

    String studyCard;

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
