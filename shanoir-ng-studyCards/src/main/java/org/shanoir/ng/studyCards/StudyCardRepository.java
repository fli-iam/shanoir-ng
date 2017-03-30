package org.shanoir.ng.studyCards;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for templates.
 *
 * @author msimon
 */
public interface StudyCardRepository extends CrudRepository<StudyCard, Long>, StudyCardRepositoryCustom {

	/**
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	Optional<StudyCard> findByName(String name);

}
