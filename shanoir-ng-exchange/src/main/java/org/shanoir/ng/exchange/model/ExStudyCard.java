package org.shanoir.ng.exchange.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * If the study card is already existing in Shanoir, the id is set,
 * if not the id == null. So we have to create a new study card in sh-ng.
 * 
 * @author mkain
 *
 */
public class ExStudyCard {
	
	/**
	 * If this id is set, an existing study card shall be used for data exchange.
	 * This is in case of an import, the study with the id shall be used.
	 * So e.g. with ShUp I would set this id and this.studyCard == null.
	 */
	@JsonProperty("id")
	private Long id;

	/**
	 * If the id == null a complete study card object as used within MS Datasets
	 * shall be added here. This can be used by an export to write a study card
	 * object into and to transfer it to another Shanoir server. The id is
	 * null, as the new server will generate a new id for this study card during
	 * the import.
	 */
//	@JsonProperty("studyCard")
//	private StudyCard studyCard;
	
	//todo: extend here for depending objects to complete the export/import

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
