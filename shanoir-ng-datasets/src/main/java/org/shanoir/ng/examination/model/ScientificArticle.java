/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
