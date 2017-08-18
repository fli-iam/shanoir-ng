package org.shanoir.ng.importer;

import java.util.List;

/**
 * Custom repository for templates.
 * 
 * @author msimon
 *
 */
public interface TemplateRepositoryCustom {

	/**
	 * Find templates by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of templates.
	 */
	List<Template> findBy(String fieldName, Object value);

}
