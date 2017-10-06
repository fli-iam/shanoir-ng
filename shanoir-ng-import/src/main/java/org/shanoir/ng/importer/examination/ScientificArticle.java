package org.shanoir.ng.importer.examination;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ScientificArticle.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "scientific_article")
@JsonPropertyOrder({ "_links", "id", "scientificArticleReference","scientificArticleType" })
public class ScientificArticle extends HalEntity {

	
	private String scientificArticleReference;
	
	private Long scientificArticleType;

	

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}



	public String getScientificArticleReference() {
		return scientificArticleReference;
	}


	public void setScientificArticleReference(String scientificArticleReference) {
		this.scientificArticleReference = scientificArticleReference;
	}



	public Long getScientificArticleType() {
		return scientificArticleType;
	}



	public void setScientificArticleType(Long scientificArticleType) {
		this.scientificArticleType = scientificArticleType;
	}




}
