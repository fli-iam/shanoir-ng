package org.shanoir.uploader.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An object that is used to import from a CSV file
 * It contains the necessary fields to be imported from CSV raw data
 * Also contains the status of the import at a time (and potential error messages)
 * @author fli
 *
 */
public class CsvImport {

	public CsvImport(String[] csvInput) {

        this.rawData = Arrays.copyOf(csvInput, 10);

        if (csvInput == null || csvInput.length < 9) {
			this.errorMessage = "shanoir.uploader.import.csv.error.column";
			return;
		}
        this.ipp = csvInput[0];
		this.name = csvInput[1];
		this.surname = csvInput[2];
		this.studyId = csvInput[3];
		this.studyCardName = csvInput[4];
		this.commonName = csvInput[5];
		this.studyFilter = csvInput[6];
		this.acquisitionFilter = csvInput[7];
		if (csvInput.length > 8) {
			this.minDateFilter = csvInput[8];
		}
		if (csvInput.length > 9) {
			this.comment = csvInput[9];
		}
	}
	
	
	String[] rawData;

	// First part is research criteria (all mandatory)

	String ipp;

	String name;
	
	String surname;
	
	// Second part is linked to import into sh-NG (all mandatory but comment)
	String studyId;
	
	String studyCardName;
	
	String commonName;
	
	String comment;
	
	String errorMessage;
	
	String acquisitionFilter;
	
	String studyFilter;
	
	/**
	 * @return the minDateFilter
	 */
	public String getMinDateFilter() {
		return minDateFilter;
	}

	/**
	 * @param minDateFilter the minDateFilter to set
	 */
	public void setMinDateFilter(String minDateFilter) {
		this.minDateFilter = minDateFilter;
	}


	String minDateFilter;

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

	/**
	 * @return the ipp
	 */
	public String getIpp() {
		return ipp;
	}

	/**
	 * @param ipp the ipp to set
	 */
	public void setIpp(String ipp) {
		this.ipp = ipp;
	}
	
}
