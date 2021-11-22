package org.shanoir.ng.dicom.web.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a DICOM-STUDY used for the DICOMweb protocol.
 * 
 * See for the standard:
 * https://dicom.nema.org/medical/dicom/current/output/html/part18.html#sect_10.4
 * 13 attributes are required by the standard, 3 are mentioned with "C", so ignored here
 * 
 * Goal is to support OHIF viewer, v2.x
 * 
 * DICOMweb:
 * WADO-RS / RetrieveStudy
 * Supported:
 * - DICOM Response, if no change in the transfer syntax
 * Not supported:
 * - DICOM Response with change in transfer syntax
 * - Bulk data response
 * - MediaType data response
 * 
 * @author mkain
 *
 */
public class StudyDTO {

	/**
	 * STUDY: 5 attributes (1 not in standard: studyDescription, but used by OHIF viewer)
	 */
	// Unique key == RootPrefix + ExaminationID
    @JsonProperty("StudyInstanceUID")
	private String studyInstanceUID;

    // == ExaminationID
    @JsonProperty("StudyID")
	private Long studyID;

    // == ExaminationComment, not mandatory by DICOMweb standard
    @JsonProperty("StudyDescription")
    private String studyDescription;
	
    // == ExaminationDate, dicom format 20010108
    @JsonProperty("StudyDate")
	private String studyDate;
	
    // == not stored today, return always 000000
    @JsonProperty("StudyTime")    
	private String studyTime;
    
    // == number of examination per patient starting with 1
    @JsonProperty("AccessionNumber")    
    private String accessionNumber;
    
    /**
     * PATIENT: 4 attributes
     */
    // subject.name == common name
    @JsonProperty("PatientName")    
	private String patientName;
	
    // == subject.id
    @JsonProperty("PatientID")    
	private String patientID;
	
    // == subject.birthDate 19800101
    @JsonProperty("PatientBirthDate")
    private String patientBirthDate;
	
    // == subject.sex, can be empty
    @JsonProperty("PatientSex")
	private String patientSex;
	
    /**
     * try if OHIF viewer works without 4 required items below:
    	ModalitiesInStudy
		ReferringPhysicianName
		NumberOfStudyRelatedSeries
		NumberOfStudyRelatedInstances
     */
    
	private List series;

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public Long getStudyID() {
		return studyID;
	}

	public void setStudyID(Long studyID) {
		this.studyID = studyID;
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public void setStudyDescription(String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public String getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(String studyDate) {
		this.studyDate = studyDate;
	}

	public String getStudyTime() {
		return studyTime;
	}

	public void setStudyTime(String studyTime) {
		this.studyTime = studyTime;
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getPatientBirthDate() {
		return patientBirthDate;
	}

	public void setPatientBirthDate(String patientBirthDate) {
		this.patientBirthDate = patientBirthDate;
	}

	public String getPatientSex() {
		return patientSex;
	}

	public void setPatientSex(String patientSex) {
		this.patientSex = patientSex;
	}

	public List getSeries() {
		return series;
	}

	public void setSeries(List series) {
		this.series = series;
	}

}
