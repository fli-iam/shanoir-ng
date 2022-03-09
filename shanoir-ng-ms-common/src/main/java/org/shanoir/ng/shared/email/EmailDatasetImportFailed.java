package org.shanoir.ng.shared.email;

public class EmailDatasetImportFailed extends EmailBase {

	private String errorMessage;

	private String subjectName;

	private String examinationId;

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
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
