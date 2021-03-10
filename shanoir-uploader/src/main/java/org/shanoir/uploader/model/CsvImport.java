package org.shanoir.uploader.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.uploader.model.rest.Sex;

/**
 * An object that is used to import from a CSV file
 * It contains the necessary fields to be imported from CSV raw data
 * Also contains the status of the import at a time (and potential error messages)
 * @author fli
 *
 */
public class CsvImport {


	public CsvImport(String[] csvInput) throws ShanoirException {

        this.rawData = Arrays.copyOf(csvInput, 10);

        if (csvInput == null || csvInput.length < 9) {
			this.errorMessage = "shanoir.uploader.import.csv.error.column";
			return;
		}
		this.name = csvInput[0];
		this.surname = csvInput[1];
		this.studyId = csvInput[2];
		this.studyCardName = csvInput[3];
		this.commonName = csvInput[4];
		try {
			this.sex = Sex.valueOf(csvInput[5]);
		} catch (IllegalArgumentException e) {
			this.errorMessage = "shanoir.uploader.import.csv.error.sex.pattern";
			return;
		}
		try {
			this.birthDate = LocalDate.parse(csvInput[6]);
		} catch (DateTimeParseException e) {
			this.errorMessage = "shanoir.uploader.import.csv.error.date.format";
			return;
		}
		this.studyFilter = csvInput[7];
		this.acquisitionFilter = csvInput[8];
		if (csvInput.length > 9) {
			this.comment = csvInput[9];
		}
	}
	
	String[] rawData;

	// First part is research criteria (all mandatory)
	String name;
	
	String surname;
	
	Date examDate;
	
	// Second part is linked to import into sh-NG (all mandatory but comment)
	String studyId;
	
	String studyCardName;
	
	String commonName;
	
	Sex sex;
	
	String comment;
	
	LocalDate birthDate;
	
	String errorMessage;
	
	String acquisitionFilter;
	
	String studyFilter;
	
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

	/**
	 * @return the birthDate
	 */
	public LocalDate getBirthDate() {
		return birthDate;
	}

	/**
	 * @param birthDate the birthDate to set
	 */
	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}
	
	/**
	 * @return the rawData
	 */
	public String[] getRawData() {
		List<String> raw = new ArrayList<>(Arrays.asList(this.rawData));
		raw.add(this.errorMessage);
		return raw.toArray(new String[0]);
	}

	/**
	 * @param rawData the rawData to set
	 */
	public void setRawData(String[] rawData) {
		this.rawData = rawData;
	}


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
	public Date getExamDate() {
		return examDate;
	}

	/**
	 * @param examDate the examDate to set
	 */
	public void setExamDate(Date examDate) {
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
	 * @return the studyCardName
	 */
	public String getStudyCardName() {
		return studyCardName;
	}

	/**
	 * @param studyCardName the studyCardName to set
	 */
	public void setStudyCardName(String studyCardName) {
		this.studyCardName = studyCardName;
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
	public Sex getSex() {
		return sex;
	}

	/**
	 * @param sex the sex to set
	 */
	public void setSex(Sex sex) {
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
	 * @return the acquisitionFilter
	 */
	public String getAcquisitionFilter() {
		return acquisitionFilter;
	}

	/**
	 * @param acquisitionFilter the acquisitionFilter to set
	 */
	public void setAcquisitionFilter(String acquisitionFilter) {
		this.acquisitionFilter = acquisitionFilter;
	}

	/**
	 * @return the studyFilter
	 */
	public String getStudyFilter() {
		return studyFilter;
	}

	/**
	 * @param studyFilter the studyFilter to set
	 */
	public void setStudyFilter(String studyFilter) {
		this.studyFilter = studyFilter;
	}
	
}
