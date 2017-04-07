package org.shanoir.ng.subject;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.study.Study;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for Subject.
 *
 * @author msimon
 */
public interface SubjectStudyRepository extends CrudRepository<SubjectStudy, Long> , SubjecStudytRepositoryCustom{

	/**
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	Optional<List<SubjectStudy>> findByStudy(Study study);
}
