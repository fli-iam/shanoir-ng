package org.shanoir.ng.studycard.dto;

/**
 * This class contains the result of an application of a study card
 * on an entire study. For each examination, when the result is wrong
 * on the examination level already, we will not check deeper on the
 * acquisition level. When everything is clear on the examination level,
 * we check deeper on the acquisition level, if there is an error, we stop.
 * Same for the dataset level.
 * 
 * In the idea, that it does not make sense to display an error on a dataset,
 * when there is already a sequence missing on the exam. Display, that at first
 * and then display the dataset result, when that problem solved?
 * 
 * @author mkain
 *
 */
public class StudyCardOnStudyResult {

	private String subjectName;
	
	private String examinationDate;
	
	private String examinationComment;
	
	private String resultExaminationLevel;
	
	private String resultAcquisitionLevel;
	
	private String resultDatasetLevel;

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getExaminationDate() {
		return examinationDate;
	}

	public void setExaminationDate(String examinationDate) {
		this.examinationDate = examinationDate;
	}

	public String getExaminationComment() {
		return examinationComment;
	}

	public void setExaminationComment(String examinationComment) {
		this.examinationComment = examinationComment;
	}

	public String getResultExaminationLevel() {
		return resultExaminationLevel;
	}

	public void setResultExaminationLevel(String resultExaminationLevel) {
		this.resultExaminationLevel = resultExaminationLevel;
	}

	public String getResultAcquisitionLevel() {
		return resultAcquisitionLevel;
	}

	public void setResultAcquisitionLevel(String resultAcquisitionLevel) {
		this.resultAcquisitionLevel = resultAcquisitionLevel;
	}

	public String getResultDatasetLevel() {
		return resultDatasetLevel;
	}

	public void setResultDatasetLevel(String resultDatasetLevel) {
		this.resultDatasetLevel = resultDatasetLevel;
	}
	
}
