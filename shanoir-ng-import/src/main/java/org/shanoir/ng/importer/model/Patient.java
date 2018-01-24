package org.shanoir.ng.importer.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */

public class Patient {
	
     @JsonProperty("subjects")
     private List<Subject> subjects;
     
     @JsonProperty("patientID")
     private String patientID;

     @JsonProperty("patientName")
     private String patientName;

     @JsonProperty("patientBirthDate")
     private String patientBirthDate;

     @JsonProperty("patientSex")
     private String patientSex;

     @JsonProperty("studies")
     private List<Study> studies;
     
     @JsonProperty("frontSubjectId")
     private Long frontSubjectId;

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
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

	public List<Study> getStudies() {
		return studies;
	}

	public void setStudies(List<Study> studies) {
		this.studies = studies;
	}

	public Long getFrontSubjectId() {
		return frontSubjectId;
	}

	public void setFrontSubjectId(Long frontSubjectId) {
		this.frontSubjectId = frontSubjectId;
	}

	public List<Subject> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<Subject> subjects) {
		this.subjects = subjects;
	}

}
