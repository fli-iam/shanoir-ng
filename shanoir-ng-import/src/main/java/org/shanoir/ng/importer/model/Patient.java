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

package org.shanoir.ng.importer.model;

import java.time.LocalDate;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.utils.Utils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a patient based on DICOM as used in Shanoir.
 *
 * @author atouboul
 * @author mkain
 */
public class Patient {

	@JsonProperty("patientID")
	private String patientID;

	@JsonProperty("patientName")
	private String patientName;

	@JsonProperty("patientLastName")
	private String patientLastName;

	@JsonProperty("patientFirstName")
	private String patientFirstName;

	@JsonProperty("patientBirthName")
	private String patientBirthName;

	@JsonProperty("patientBirthDate")
	@LocalDateAnnotations
	private LocalDate patientBirthDate;

	@JsonProperty("patientSex")
	private String patientSex;

	@JsonProperty("patientIdentityRemoved")
	private boolean patientIdentityRemoved;

	@JsonProperty("deIdentificationMethod")
	private String deIdentificationMethod;

	@JsonProperty("subject")
	private Subject subject;

	// Keep this empty constructor to avoid Jackson deserialization exceptions
	public Patient() {
	}

	public Patient(final Attributes attributes) {
		this.patientID = attributes.getString(Tag.PatientID);
		this.patientName = attributes.getString(Tag.PatientName);
		this.patientBirthName = attributes.getString(Tag.PatientBirthName);
		this.patientBirthDate = DateTimeUtils.dateToLocalDate(attributes.getDate(Tag.PatientBirthDate));
		this.patientSex = attributes.getString(Tag.PatientSex);
		splitPatientName(this.patientName);
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

	public boolean isPatientIdentityRemoved() {
		return patientIdentityRemoved;
	}

	public void setPatientIdentityRemoved(boolean patientIdentityRemoved) {
		this.patientIdentityRemoved = patientIdentityRemoved;
	}

	public String getDeIdentificationMethod() {
		return deIdentificationMethod;
	}

	public void setDeIdentificationMethod(String deIdentificationMethod) {
		this.deIdentificationMethod = deIdentificationMethod;
	}

	public String getPatientLastName() {
		return patientLastName;
	}

	public void setPatientLastName(String patientLastName) {
		this.patientLastName = patientLastName;
	}

	public String getPatientFirstName() {
		return patientFirstName;
	}

	public void setPatientFirstName(String patientFirstName) {
		this.patientFirstName = patientFirstName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Patient [");
		if (patientID != null) {
			sb.append("patientID=").append(Utils.sha256(patientID)).append(", ");
		}
		if (patientName != null) {
			sb.append("patientname = ").append(Utils.sha256(patientName)).append(", ");
		}
		if (patientBirthName != null) {
			sb.append("patientBirthname = ").append(Utils.sha256(patientBirthName)).append(", ");
		}
		if (patientBirthDate != null) {
			sb.append("patientBirthDate=").append(Utils.sha256(patientBirthDate.toString())).append(", ");
		}
		if (sb.lastIndexOf(", ") == sb.length() - 2) {
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append("]");
		return sb.toString();
	}

	public String toTreeString() {
		return patientName + " [patientID=" + patientID + ", patientBirthDate=" + patientBirthDate + "]";
	}

	private void splitPatientName(String patientName) {
		if (patientName == null || patientName.isEmpty()) {
			this.patientLastName = "";
			this.patientFirstName = "";
			return;
		}
		// DICOM names are encoded as LastName^FirstName^MiddleName^Prefix^Suffix
		String[] nameParts = patientName.split("\\^");
		this.patientLastName = nameParts.length > 0 ? nameParts[0].trim() : "";
		this.patientFirstName = nameParts.length > 1 ? nameParts[1].trim() : "";
		// Handle cases where name might have been entered as "FirstName LastName"
		if (this.patientLastName.isEmpty() && this.patientFirstName.contains(" ")) {
			String[] parts = this.patientFirstName.split(" ", 2);
			this.patientFirstName = parts.length > 0 ? parts[0].trim() : "";
			this.patientLastName = parts.length > 1 ? parts[1].trim() : "";
		}
		// If birth name is missing in DICOM: use last name by default
		// Users can adapt it in PatientVerification on using ShUp
		if (patientBirthName == null || patientBirthName.isEmpty()) {
			this.patientBirthName = this.patientLastName;
		}
	}

}
