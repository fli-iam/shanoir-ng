package org.shanoir.ng.template;

import java.util.Optional;

import org.shanoir.ng.shared.model.ItemRepositoryCustom;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for templates.
 *
 * @author msimon
 */
public interface TemplateRepository extends CrudRepository<Template, Long>, ItemRepositoryCustom<Template> {

	/**
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	Optional<Template> findByData(String data);

}
