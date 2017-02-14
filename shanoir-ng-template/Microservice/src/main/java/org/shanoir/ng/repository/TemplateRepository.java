package org.shanoir.ng.repository;

import java.util.Optional;

import org.shanoir.ng.model.Template;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for templates.
 *
 * @author msimon
 */
public interface TemplateRepository extends CrudRepository<Template, Long>, TemplateRepositoryCustom {

	/**
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	Optional<Template> findByData(String data);

}
