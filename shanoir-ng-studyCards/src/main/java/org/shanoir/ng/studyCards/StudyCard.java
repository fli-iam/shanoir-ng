package org.shanoir.ng.studyCards;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Template.
 * 
 * @author msimon
 *
 */
@Entity
@Table(name = "study_cards")
@JsonPropertyOrder({ "_links", "id", "name", "isDisabled" })
public class StudyCard extends HalEntity {

	private String name;
	
	private boolean isDisabled = false;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "studyCard/" + getId());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisabled() {
		return isDisabled;
	}

	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}
	
	

}
