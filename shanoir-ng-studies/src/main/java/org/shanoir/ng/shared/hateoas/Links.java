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
	
	public Links() {
		
	}
}
