package org.shanoir.ng.importer.dto;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

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
    @LocalDateAnnotations
    private LocalDate patientBirthDate;

    @JsonProperty("patientSex")
    private String patientSex;

    @JsonProperty("studies")
    private List<Study> studies;

    @JsonProperty("frontExperimentalGroupOfSubjectId")
    private Long frontExperimentalGroupOfSubjectId;
    
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

	public Long getFrontExperimentalGroupOfSubjectId() {
		return frontExperimentalGroupOfSubjectId;
	}

	public void setFrontExperimentalGroupOfSubjectId(Long frontExperimentalGroupOfSubjectId) {
		this.frontExperimentalGroupOfSubjectId = frontExperimentalGroupOfSubjectId;
	}

}
