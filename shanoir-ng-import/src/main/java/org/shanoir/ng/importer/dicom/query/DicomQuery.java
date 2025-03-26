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

package org.shanoir.ng.importer.dicom.query;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * This class contains the C-FIND query attributes entered
 * by the user and send to the PACS to retrieve entities.
 * By default, patient root query is used, that is why the
 * attribute studyRootQuery is false by default.
 *
 * @author mkain
 *
 */
public class DicomQuery {

	@NotNull
	@Size(max = 64)
	@JsonProperty("patientName")
	private String patientName;

	@NotNull
	@Size(max = 64)
	@JsonProperty("patientID")
	private String patientID;

	@NotNull
	@Size(max = 8)
	@JsonProperty("patientBirthDate")
	private String patientBirthDate;

	@NotNull
	@Size(max = 64)
	@JsonProperty("studyDescription")
	private String studyDescription;

	@NotNull
	@Size(max = 8)
	@JsonProperty("studyDate")
	private String studyDate;

	@JsonProperty("modality")
	private String modality;

	// default is patient root query
	@JsonProperty("studyRootQuery")
	private boolean studyRootQuery;

	private	String studyFilter;

	private String minStudyDateFilter;

	private String serieFilter;

	public String getPatientName() {
		return patientName;
	}

	public String getPatientID() {
		return patientID;
	}

	public String getPatientBirthDate() {
		return patientBirthDate;
	}

	public boolean isStudyRootQuery() {
		return studyRootQuery;
	}

	public void setStudyRootQuery(boolean studyRootQuery) {
		this.studyRootQuery = studyRootQuery;
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

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String[] displayDicomQuery() {
		String queryLevel = null;
		if (studyRootQuery) {
			queryLevel = "STUDY";
		} else {
			queryLevel = "PATIENT";
		}
		return new String[] {
				queryLevel,
				patientName,
				patientID,
				patientBirthDate,
				studyDescription,
				studyDate,
				modality,
				studyFilter,
				minStudyDateFilter,
				serieFilter
		};
	}

	public String getStudyFilter() {
		return studyFilter;
	}

	public void setStudyFilter(String studyFilter) {
		this.studyFilter = studyFilter;
	}

	public String getMinStudyDateFilter() {
		return minStudyDateFilter;
	}

	public void setMinStudyDateFilter(String minStudyDateFilter) {
		this.minStudyDateFilter = minStudyDateFilter;
	}

	public String getSerieFilter() {
		return serieFilter;
	}

	public void setSerieFilter(String serieFilter) {
		this.serieFilter = serieFilter;
	}

	@Override
	public String toString() {
		return "DicomQuery [patientName=" + patientName + ", patientID=" + patientID + ", patientBirthDate="
				+ patientBirthDate + ", studyDescription=" + studyDescription + ", studyDate=" + studyDate
				+ ", modality=" + modality + ", studyRootQuery=" + studyRootQuery + ", studyFilter=" + studyFilter
				+ ", minStudyDateFilter=" + minStudyDateFilter + ", serieFilter=" + serieFilter + "]";
	}

}
