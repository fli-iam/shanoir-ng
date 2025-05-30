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
import java.util.stream.Collectors;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a study based on DICOM as used in Shanoir.
 * 
 * @author atouboul
 * @author mkain
 */
public class Study {

	@JsonProperty("studyInstanceUID")
	private String studyInstanceUID;

	@JsonProperty("studyDate")
	@LocalDateAnnotations
	private LocalDate studyDate;

	@JsonProperty("studyDescription")
	private String studyDescription;

	@JsonProperty("series")
	private List<Serie> series;

	// Keep this empty constructor to avoid Jackson deserialization exceptions
	public Study() {}

	public Study(final Attributes attributes) {
		studyInstanceUID = attributes.getString(Tag.StudyInstanceUID);
		// try to remove confusing spaces, in case DICOM server sends them wrongly
		if (studyInstanceUID != null)
			studyInstanceUID = studyInstanceUID.trim();
		studyDate = DateTimeUtils.dateToLocalDate(attributes.getDate(Tag.StudyDate));
		studyDescription = attributes.getString(Tag.StudyDescription);
	}

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public LocalDate getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(LocalDate studyDate) {
		this.studyDate = studyDate;
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public void setStudyDescription(String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public List<Serie> getSeries() {
		return series;
	}

	@JsonIgnore
	public List<Serie> getSelectedSeries() {
		return series.stream().filter(Serie::getSelected).collect(Collectors.toList());
	}

	public void setSeries(List<Serie> series) {
		this.series = series;
	}

	@Override
	public String toString() {
		return "Study [studyInstanceUID=" + studyInstanceUID + ", studyDate=" + studyDate + ", studyDescription="
				+ studyDescription + "]";
	}

	public String toTreeString() {
		return "[" + studyDate + "] " + studyDescription + " [number of series=" + series.size() +", studyInstanceUID=" + studyInstanceUID + "]";
	}

}