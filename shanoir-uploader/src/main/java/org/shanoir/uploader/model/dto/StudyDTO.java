package org.shanoir.uploader.model.dto;

import java.util.List;

public class StudyDTO {

	private Long id;
	
	private String name;
	
	private List<StudyCardDTO> studyCards;
	
	private List<CenterDTO> centers;
	
	public StudyDTO(Long id, String name, List<StudyCardDTO> studyCards, List<CenterDTO> centers) {
		super();
		this.id = id;
		this.name = name;
		this.studyCards = studyCards;
		this.centers = centers;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
    public List<StudyCardDTO> getStudyCards() {
		return studyCards;
	}

	public void setStudyCards(List<StudyCardDTO> studyCards) {
		this.studyCards = studyCards;
	}

	public List<CenterDTO> getCenters() {
		return centers;
	}

	public void setCenters(List<CenterDTO> centers) {
		this.centers = centers;
	}
	
}
