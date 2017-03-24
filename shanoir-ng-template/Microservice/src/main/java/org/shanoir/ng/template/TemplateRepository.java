package org.shanoir.ng.template;

import java.util.Optional;

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
