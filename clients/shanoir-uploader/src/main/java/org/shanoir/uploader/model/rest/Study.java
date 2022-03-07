package org.shanoir.uploader.model.rest;

import java.util.Collections;
import java.util.List;

import org.shanoir.uploader.ShUpConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Study implements Comparable<Study> {

	private Long id;

	private String name;

	private List<StudyCard> studyCards;

	@JsonProperty("studyCenterList")
	private List<Center> centers;

	private Boolean compatible;

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

	public List<StudyCard> getStudyCards() {
		return studyCards;
	}

	public void setStudyCards(List<StudyCard> studyCards) {
		this.studyCards = studyCards;
	}

	public List<Center> getCenters() {
		return centers;
	}

	public void setCenters(List<Center> centers) {
		this.centers = centers;
		Collections.sort(this.centers);
	}

	public Boolean getCompatible() {
		return compatible;
	}

	public void setCompatible(Boolean compatible) {
		this.compatible = compatible;
	}

	public String toString() {
		if (this.getStudyCards() != null && !this.getStudyCards().isEmpty()) {
			if (compatible) {
				return ShUpConfig.resourceBundle.getString("shanoir.uploader.import.compatible") + " " + this.getName();
			} else {
				return this.getName();			
			}
		} else {
			return this.getName() + ", 0 study card.";
		}
	}

	public int compareTo(Study o) {
		return Long.compare(this.getId(), o.getId());
	}

}
