package org.shanoir.ng.repository;

import java.util.List;

import org.shanoir.ng.model.Template;

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
