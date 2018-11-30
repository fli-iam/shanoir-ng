package org.shanoir.ng.importer.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 * @author mkain
 *
 */
public class Patient {
	
	private static final String NO_ID = "No ID";

	@JsonProperty("patientID")
	private String patientID;

	@JsonProperty("patientName")
	private String patientName;

	@JsonProperty("patientBirthName")
	private String patientBirthName;

	@JsonProperty("patientBirthDate")
	private Date patientBirthDate;

	@JsonProperty("patientSex")
	private String patientSex;

	@JsonProperty("subject")
	private Subject subject;

	public Patient(String patientID, String patientName, String patientBirthName, Date patientBirthDate,
			String patientSex) {
		if (patientID == null || "".equals(patientID)) {
			this.patientID = NO_ID;
		} else {
			this.patientID = patientID;			
		}
		this.patientName = patientName;
		this.patientBirthName = patientBirthName;
		this.patientBirthDate = patientBirthDate;
		this.patientSex = patientSex;
	}

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

	public String getPatientBirthName() {
		return patientBirthName;
	}

	public void setPatientBirthName(String patientBirthName) {
		this.patientBirthName = patientBirthName;
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
