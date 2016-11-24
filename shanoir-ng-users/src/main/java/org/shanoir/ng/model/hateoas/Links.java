package org.shanoir.ng.model.hateoas;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = LinksSerializer.class)
public class Links {


	public static final String REL_SELF = "self";
	public static final String REL_FIRST = "first";
	public static final String REL_PREVIOUS = "prev";
	public static final String REL_NEXT = "next";
	public static final String REL_LAST = "last";

	private List<Link> links = new ArrayList<Link>();

	/**
	 * @return the links
	 */
	protected List<Link> getLinks() {
		return links;
	}

	/**
	 * @param links the links to set
	 */
	protected void setLinks(List<Link> links) {
		this.links = links;
	}




}
