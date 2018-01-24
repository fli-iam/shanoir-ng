package org.shanoir.ng.importer.dto;

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
     private String studyDate;

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

	public String getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(String studyDate) {
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
