package org.shanoir.ng.importer.model;

import java.time.LocalDate;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a patient based on Dicom as used in Shanoir.
 * 
 * @author atouboul
 * @author mkain
 */
public class Patient {

	@JsonProperty("patientID")
	private String patientID;

	@JsonProperty("patientName")
	private String patientName;

	@JsonProperty("patientBirthName")
	private String patientBirthName;

	@JsonProperty("patientBirthDate")
	@LocalDateAnnotations
	private LocalDate patientBirthDate;

	@JsonProperty("patientSex")
	private String patientSex;

	@JsonProperty("subject")
	private Subject subject;

	// Keep this empty constructor to avoid Jackson deserialization exceptions
	public Patient() {}

	public Patient(final Attributes attributes) {
		this.patientID = attributes.getString(Tag.PatientID);
		this.patientName = attributes.getString(Tag.PatientName);
		this.patientBirthName = attributes.getString(Tag.PatientBirthName);
		this.patientBirthDate = DateTimeUtils.dateToLocalDate(attributes.getDate(Tag.PatientBirthDate));
		this.patientSex = attributes.getString(Tag.PatientSex);
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

	public LocalDate getPatientBirthDate() {
		return patientBirthDate;
	}

	public void setPatientBirthDate(LocalDate patientBirthDate) {
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
