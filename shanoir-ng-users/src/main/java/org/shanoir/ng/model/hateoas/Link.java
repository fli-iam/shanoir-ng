package org.shanoir.ng.model.hateoas;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = LinkSerializer.class)
public class Link {

	/** The relationship. */
	private String rel;

	/** The link url. */
	private HRef href;


	/**
	 * @param rel
	 * @param relativePath
	 */
	public Link(String rel, HRef href) {
		super();
		this.rel = rel;
		this.href = href;
	}

	/**
	 * @return the rel
	 */
	protected String getRel() {
		return rel;
	}

	/**
	 * @param rel the rel to set
	 */
	protected void setRel(String rel) {
		this.rel = rel;
	}

	/**
	 * @return the href
	 */
	protected HRef getHref() {
		return href;
	}

	/**
	 * @param href the href to set
	 */
	protected void setHref(HRef href) {
		this.href = href;
	}

}
