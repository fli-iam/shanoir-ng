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

package org.shanoir.ng.template;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirTemplateException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

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
