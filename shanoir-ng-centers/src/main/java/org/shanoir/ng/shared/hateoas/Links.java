package org.shanoir.ng.shared.hateoas;

import java.util.HashMap;

/**
 * List of Hateoas links.
 * 
 * @author jlouis
 *
 */
public class Links extends HashMap<String, Link> {

	private static final long serialVersionUID = 1L;

	public static final String REL_SELF = "self";
	public static final String REL_FIRST = "first";
	public static final String REL_PREVIOUS = "prev";
	public static final String REL_NEXT = "next";
	public static final String REL_LAST = "last";

	public static final String BASE_URL = "http://localhost:9900/";

}
