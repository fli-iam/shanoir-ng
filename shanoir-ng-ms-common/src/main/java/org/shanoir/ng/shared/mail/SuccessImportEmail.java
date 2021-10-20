package org.shanoir.ng.shared.mail;

import java.util.Map;

/**
 * This class represents an EMAIL to be sent after an email was imported.
 * @author JCD
 *
 */
public class SuccessImportEmail extends ImportEmail {

    Map<Long, String> datasets;

    String examDate;

    String studyCard;

    String examinationId;

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
