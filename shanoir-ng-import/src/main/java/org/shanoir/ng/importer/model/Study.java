package org.shanoir.ng.importer.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */
public class Study {

     @JsonProperty("studyInstanceUID")
     private String studyInstanceUID;

     @JsonProperty("studyDate")
     private Date studyDate;

     @JsonProperty("studyDescription")
     private String studyDescription;

     @JsonProperty("series")
     private List<Serie> series;

	public Study(String studyInstanceUID, Date studyDate, String studyDescription) {
		this.studyInstanceUID = studyInstanceUID;
		this.studyDate = studyDate;
		this.studyDescription = studyDescription;
	}

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public Date getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(Date studyDate) {
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