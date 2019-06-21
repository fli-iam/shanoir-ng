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

/**
 * Scientific article type.
 * 
 * @author ifakhfakh
 *
 */
public enum ScientificArticleType {

	/***
	 * instrument_definition_article.
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
	public static ScientificArticleType getType(final Integer id) {
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
