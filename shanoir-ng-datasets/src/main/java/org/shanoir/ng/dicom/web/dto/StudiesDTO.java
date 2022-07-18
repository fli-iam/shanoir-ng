package org.shanoir.ng.dicom.web.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudiesDTO {

    @JsonProperty("studies")   
	private List<StudyDTO> studies;

	public List<StudyDTO> getStudies() {
		return studies;
	}

	public void setStudies(List<StudyDTO> studies) {
		this.studies = studies;
	}
	
}
