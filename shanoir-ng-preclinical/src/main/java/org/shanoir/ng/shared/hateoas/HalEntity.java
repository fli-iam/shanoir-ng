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

package org.shanoir.ng.shared.hateoas;

import org.shanoir.ng.shared.model.AbstractGenericItem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Helps to format an entity to the HAL format
 *
 * @author jlouis
 */
public abstract class HalEntity extends AbstractGenericItem {

	@JsonIgnore
	private Links links = new Links();

	/**
	 * @return the links
	 */
	@JsonProperty("_links")
	public Links getLinks() {
		return links;
	}

	/**
	 * @param links the links to set
	 */
	@JsonIgnore
	public void setLinks(Links links) {
		this.links = links;
	}


	public void addLink(String key, String href) {
		links.put(key, new Link(key, Links.BASE_URL + href));
	}

}
