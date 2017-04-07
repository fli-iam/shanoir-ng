package org.shanoir.ng.subject;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for Subject.
 *
 * @author msimon
 */
public interface SubjectRepository extends CrudRepository<Subject, Long>, SubjectRepositoryCustom {

	/**
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	Optional<Subject> findByName(String name);

}
