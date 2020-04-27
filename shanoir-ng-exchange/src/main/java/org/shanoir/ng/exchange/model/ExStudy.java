package org.shanoir.ng.exchange.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * If the study is already existing in Shanoir, the studyName is set,
 * if not the studyName == null. So we have to create a new study in sh-ng.
 * 
 * @author mkain
 *
 */
public class ExStudy {
	
	/**
	 * If the study name is set, an existing study shall be used for data exchange.
	 * In case of an import, the study with the name shall be used.
	 * So e.g. with ShUp I would set this name and this.study == null.
	 * The study name is unique within sh-ng.
	 */
	@JsonProperty("studyName")
	private String studyName;
	
	/**
	 * If the id == null a complete study object as used within MS Studies
	 * shall be added here. This can be used by an export to write a study
	 * object into and to transfer it to another Shanoir server. The id is
	 * null, as the new server will generate a new id for this study during
	 * the import.
	 */
//	@JsonProperty("study")
//	private Study study;
	
	/**
	 * At least one ExStudyCard needs to be present to exchange data.
	 * In case of an import, for the moment always the first study card
	 * object is used for all subjects and studies(dicom)==examinations.
	 */
	@JsonProperty("exStudyCards")
	private List<ExStudyCard> exStudyCards;
	
	/**
	 * At least one ExSubject needs to be present to exchange data.
	 */
	@JsonProperty("exSubjects")
	private List<ExSubject> exSubjects;

	public List<ExStudyCard> getExStudyCards() {
		return exStudyCards;
	}

	public List<ExSubject> getExSubjects() {
		return exSubjects;
	}

	public void setExStudyCards(List<ExStudyCard> exStudyCards) {
		this.exStudyCards = exStudyCards;
	}

	public void setExSubjects(List<ExSubject> exSubjects) {
		this.exSubjects = exSubjects;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

}
