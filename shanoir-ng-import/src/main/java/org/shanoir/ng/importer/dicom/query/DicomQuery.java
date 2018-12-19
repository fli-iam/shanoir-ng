package org.shanoir.ng.importer.dicom.query;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ImportFromPACS: the user can query a PACS to chose
 * his series to be imported by Shanoir. This class contains
 * the query executed by the end user and send to the PACS.
 * 
 * @author mkain
 *
 */
public class DicomQuery {

	@NotNull
	@Size(max=64)
    @JsonProperty("patientName")
    private String patientName;
    
	@NotNull
	@Size(max=64)
    @JsonProperty("patientID")
    private String patientID;
    
	@NotNull
	@Size(max=8)
    @JsonProperty("patientBirthDate")
    private String patientBirthDate;
    
	@NotNull
	@Size(max=64)
    @JsonProperty("studyDescription")
    private String studyDescription;
    
	@NotNull
	@Size(max=8)
    @JsonProperty("studyDate")
    private String studyDate;

	public String getPatientName() {
		return patientName;
	}

	public String getPatientID() {
		return patientID;
	}

	public String getPatientBirthDate() {
		return patientBirthDate;
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public String getStudyDate() {
		return studyDate;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public void setPatientBirthDate(String patientBirthDate) {
		this.patientBirthDate = patientBirthDate;
	}

	public void setStudyDescription(String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public void setStudyDate(String studyDate) {
		this.studyDate = studyDate;
	}
    
}
