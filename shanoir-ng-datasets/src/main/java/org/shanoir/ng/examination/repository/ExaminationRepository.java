package org.shanoir.ng.examination.repository;

import java.util.List;

import org.shanoir.ng.examination.model.Examination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for examination.
 *
 * @author ifakhfakh
 */
public interface ExaminationRepository extends PagingAndSortingRepository<Examination, Long> {

	/**
	 * Get a paginated list of examinations for a list of studies.
	 * 
	 * @param studyIds
	 *            list of study ids.
	 * @param pageable
	 *            pagination data.
	 * @return list of examinations.
	 */
	Page<Examination> findByStudyIdIn(List<Long> studyIds, Pageable pageable);

	/**
	 * Get a list of examinations for a subject.
	 * 
	 * @param subjectId
	 * @return
	 * @author yyao
	 *            subject id.
	 * @return list of examinations.
	 */
	List<Examination> findBySubjectId(Long subjectId);
	
	/**
	 * 
	 * @param subjectId: 
	 * @param studyId
	 * @return list of examinations.
	 */
	List<Examination> findBySubjectIdAndStudyId(Long subjectId, Long studyId);
 
}
