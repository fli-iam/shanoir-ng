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


public enum ScientificArticleType {

	/***
	 *  instrument_definition_article.
	 */
	INSTRUMENT_DEFINITION_ARTICLE(1),
	
	/**
	 * score_standardisation_article.
	 */
	SCORE_STANDARDISATION_ARTICLE(2);
		

	private int id;
	

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ScientificArticleType(final int id) {
		this.id = id;
	}

	/**
	 * Get a scientific article type by its id.
	 * 
	 * @param id
	 *            scientific article type id.
	 * @return scientific article type.
	 */
	public static ScientificArticleType getScientificArticleType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ScientificArticleType scientificArticleType : ScientificArticleType.values()) {
			if (id.equals(scientificArticleType.getId())) {
				return scientificArticleType;
			}
		}
		throw new IllegalArgumentException("No matching scientific article type for id " + id);
	}

	/** 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}

