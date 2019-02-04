package org.shanoir.ng.importer.model;

import java.time.LocalDate;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a study based on Dicom as used in Shanoir.
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
		this.studyInstanceUID = attributes.getString(Tag.StudyInstanceUID);
		this.studyDate = DateTimeUtils.dateToLocalDate(attributes.getDate(Tag.StudyDate));
		this.studyDescription = attributes.getString(Tag.StudyDescription);
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

	public void setSeries(List<Serie> series) {
		this.series = series;
	}

}