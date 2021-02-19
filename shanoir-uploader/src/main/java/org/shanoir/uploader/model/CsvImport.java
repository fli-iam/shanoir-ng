package org.shanoir.uploader.model;

/**
 * An object that is used to import from a CSV file
 * It contains the necessary fields to be imported from CSV raw data
 * Also contains the status of the import at a time (and potential error messages)
 * @author fli
 *
 */
public class CsvImport {

	public CsvImport(String[] csvInput) {
		if (csvInput == null || csvInput.length < 7) {
			this.error = "Not enough columns in CSV, please check it";
			return;
		}
		this.rawData = csvInput;
		this.name = csvInput[0];
		this.surname = csvInput[1];
		this.examDate = csvInput[2];
		this.studyId = csvInput[3];
		this.studyCardId = csvInput[4];
		this.commonName = csvInput[5];
		this.sex = csvInput[6];
		if (csvInput.length > 7) {
			this.comment = csvInput[7];
		}
	}
	
	/**
	 * @return the rawData
	 */
	public String[] getRawData() {
		return rawData;
	}

	/**
	 * @param rawData the rawData to set
	 */
	public void setRawData(String[] rawData) {
		this.rawData = rawData;
	}

	String[] rawData;

	// First part is research criteria (all mandatory)
	String name;
	
	String surname;
	
	String examDate;
	
	// Second part is linked to import into sh-NG (all mandatory but comment)
	String studyId;
	
	String studyCardId;
	
	String commonName;
	
	String sex;
	
	String comment;
	
	// Finally a displayed error
	String error;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
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
	 * @return the studyCardId
	 */
	public String getStudyCardId() {
		return studyCardId;
	}

	/**
	 * @param studyCardId the studyCardId to set
	 */
	public void setStudyCardId(String studyCardId) {
		this.studyCardId = studyCardId;
	}

	/**
	 * @return the commonName
	 */
	public String getCommonName() {
		return commonName;
	}

	/**
	 * @param commonName the commonName to set
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * @return the sex
	 */
	public String getSex() {
		return sex;
	}

	/**
	 * @param sex the sex to set
	 */
	public void setSex(String sex) {
		this.sex = sex;
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
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}
	
}
