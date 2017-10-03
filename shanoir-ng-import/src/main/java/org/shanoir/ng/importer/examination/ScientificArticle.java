package org.shanoir.ng.importer.examination;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@JsonPropertyOrder({ "_links", "id", "scientificArticle","scientificArticleType" })
public class ScientificArticle extends HalEntity {

	
	private String scientificArticle;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scientificArticleType",updatable = true, nullable = false)
	private ScientificArticleType scientificArticleType;

	

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}



	public String getScientificArticle() {
		return scientificArticle;
	}



	public void setScientificArticle(String scientificArticle) {
		this.scientificArticle = scientificArticle;
	}



	public ScientificArticleType getScientificArticleType() {
		return scientificArticleType;
	}



	public void setScientificArticleType(ScientificArticleType scientificArticleType) {
		this.scientificArticleType = scientificArticleType;
	}

}
