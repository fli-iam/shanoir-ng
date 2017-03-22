package org.shanoir.ng.shared.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author jlouis
 */
public class Link {

	@JsonIgnore
	private String rel;

	private String href;

	/**
	 * @param rel
	 * @param href
	 */
	public Link(String rel, String href) {
		this.rel = rel;
		this.href = href;
	}

	/**
	 * @return the rel
	 */
	public String getRel() {
		return rel;
	}

	/**
	 * @param rel the rel to set
	 */
	public void setRel(String rel) {
		this.rel = rel;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @param href the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

}
