package org.shanoir.ng.subjectstudy;

import java.util.List;

import org.shanoir.ng.study.Study;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for Subject.
 *
 * @author msimon
 */
public interface SubjectStudyRepository extends CrudRepository<SubjectStudy, Long> {

	/**
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	List<SubjectStudy> findByStudy(Study study);
	
}
