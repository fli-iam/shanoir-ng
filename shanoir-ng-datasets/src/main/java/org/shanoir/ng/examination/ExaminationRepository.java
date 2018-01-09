package org.shanoir.ng.examination;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for examination.
 *
 * @author ifakhfakh
 */
public interface ExaminationRepository extends CrudRepository<Examination, Long> {

	/**
	 * @param subjectId
	 * @return
	 * @author yyao
	 */
	List<Examination> findBySubjectId(Long subjectId);

}
