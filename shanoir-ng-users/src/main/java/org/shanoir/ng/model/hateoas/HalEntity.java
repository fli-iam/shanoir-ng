package org.shanoir.ng.model.hateoas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Helps to format an entity to the HAL format
 *
 * @author jlouis
 */
public abstract class HalEntity {


	@JsonProperty("_links")
	private Links links = new Links();


	/**
	 * @return the links
	 */
	public Links getLinks() {
		return links;
	}


	/**
	 * @param links the links to set
	 */
	public void setLinks(Links links) {
		this.links = links;
	}


	public void addLink(String key, String href) {
		links.put(key, new Link(key, Links.BASE_URL + href));
	}

}
