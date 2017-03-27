package org.shanoir.ng.utils;

import org.shanoir.ng.center.Center;

/**
 * Utility class for test.
 * Generates models.
 * 
 * @author msimon
 *
 */
public final class ModelsUtil {

	// Center data
	public static final String CENTER_NAME = "tt";
	
	/**
	 * Create a center.
	 * 
	 * @return center.
	 */
	public static Center createCenter() {
		final Center center = new Center();
		center.setName(CENTER_NAME);
		return center;
	}
	
}
