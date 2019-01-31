package org.shanoir.ng.importer.dto;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author atouboul
 *
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
