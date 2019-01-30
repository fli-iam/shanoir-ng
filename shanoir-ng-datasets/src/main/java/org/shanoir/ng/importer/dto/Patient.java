/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.dto;

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

	public Long getFrontExperimentalGroupOfSubjectId() {
		return frontExperimentalGroupOfSubjectId;
	}

	public void setFrontExperimentalGroupOfSubjectId(Long frontExperimentalGroupOfSubjectId) {
		this.frontExperimentalGroupOfSubjectId = frontExperimentalGroupOfSubjectId;
	}

}
