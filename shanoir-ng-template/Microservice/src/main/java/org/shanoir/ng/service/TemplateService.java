package org.shanoir.ng.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.exception.ShanoirTemplateException;
import org.shanoir.ng.model.Template;
import org.shanoir.ng.model.validation.UniqueCheckableService;

/**
 * Template service.
 *
 * @author msimon
 *
 */
public interface TemplateService extends UniqueCheckableService<Template> {

	/**
	 * Delete a template.
	 * 
	 * @param id
	 *            template id.
	 * @throws ShanoirTemplateException
	 */
	void deleteById(Long id) throws ShanoirTemplateException;

	/**
	 * Get all the template.
	 * 
	 * @return a list of templates.
	 */
	List<Template> findAll();

	/**
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	Optional<Template> findByData(String data);

	/**
	 * Find template by its id.
	 *
	 * @param id
	 *            template id.
	 * @return a template or null.
	 */
	Template findById(Long id);

	/**
	 * Save a template.
	 *
	 * @param template
	 *            template to create.
	 * @return created template.
	 * @throws ShanoirTemplateException
	 */
	Template save(Template template) throws ShanoirTemplateException;

	/**
	 * Update a template.
	 *
	 * @param template
	 *            template to update.
	 * @return updated template.
	 * @throws ShanoirTemplateException
	 */
	Template update(Template template) throws ShanoirTemplateException;

	/**
	 * Update a template from the old Shanoir
	 * 
	 * @param template
	 *            template.
	 * @throws ShanoirTemplateException
	 */
	void updateFromShanoirOld(Template template) throws ShanoirTemplateException;

}
