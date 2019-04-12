package org.shanoir.ng.examination.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * Scientific article.
 * 
 * @author ifakhfakh
 *
 */
@Entity
public class ScientificArticle extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 250233615450555267L;

	/** The reference to the article. */
	@NotNull
	private String scientificArticleReference;

	/**
	 * the type of article (instrument definition, score standardization, etc.).
	 */
	private Integer scientificArticleType;

	/**
	 * @return the scientificArticleReference
	 */
	public String getScientificArticleReference() {
		return scientificArticleReference;
	}

	/**
	 * @param scientificArticleReference
	 *            the scientificArticleReference to set
	 */
	public void setScientificArticleReference(String scientificArticleReference) {
		this.scientificArticleReference = scientificArticleReference;
	}

	/**
	 * @return the scientificArticleType
	 */
	public ScientificArticleType getScientificArticleType() {
		return ScientificArticleType.getType(scientificArticleType);
	}

	/**
	 * @param scientificArticleType
	 *            the scientificArticleType to set
	 */
	public void setScientificArticleType(ScientificArticleType scientificArticleType) {
		if (scientificArticleType == null) {
			this.scientificArticleType = null;
		} else {
			this.scientificArticleType = scientificArticleType.getId();
		}
	}

}
