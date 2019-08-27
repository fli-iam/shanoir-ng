package org.shanoir.uploader.model.dto.rest.importmodel;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */

public class Patient {
	
     @JsonProperty("subject")
     private Subject subject;
     
     @JsonProperty("patientID")
     private String patientID;

     @JsonProperty("patientName")
     private String patientName;

     @JsonProperty("patientBirthDate")
     private Date patientBirthDate;

     @JsonProperty("patientSex")
     private String patientSex;

     @JsonProperty("studies")
     private List<Study> studies;
     
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

	public Date getPatientBirthDate() {
		return patientBirthDate;
	}

	public void setPatientBirthDate(Date patientBirthDate) {
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

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

}
