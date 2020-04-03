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

package org.shanoir.ng.exchange.model.dicom;

import java.time.LocalDate;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a DICOM patient in Shanoir.
 * 
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

	@JsonProperty("studies")
	private List<Study> studies;

	// Keep this empty constructor to avoid Jackson deserialization exceptions
	public Patient() {}

	public Patient(final Attributes attributes) {
		this.patientID = attributes.getString(Tag.PatientID);
		this.patientName = attributes.getString(Tag.PatientName);
		this.patientBirthName = attributes.getString(Tag.PatientBirthName);
		this.patientBirthDate = DateTimeUtils.dateToLocalDate(attributes.getDate(Tag.PatientBirthDate));
		this.patientSex = attributes.getString(Tag.PatientSex);
	}

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

}
