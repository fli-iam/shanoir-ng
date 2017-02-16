package org.shanoir.ng.utils;

import org.shanoir.ng.model.Template;

/**
 * Utility class for test.
 * Generates models.
 * 
 * @author msimon
 *
 */
public final class ModelsUtil {

	// Template data
	public static final String TEMPLATE_DATA = "data";
	
	/**
	 * Create a template.
	 * 
	 * @return template.
	 */
	public static Template createTemplate() {
		final Template template = new Template();
		template.setData(TEMPLATE_DATA);
		return template;
	}
	
}
