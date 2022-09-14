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

package org.shanoir.ng.examination.repository;

import java.util.List;

import org.shanoir.ng.examination.model.Examination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for examination.
 *
 * @author ifakhfakh
 */
public interface ExaminationRepository extends PagingAndSortingRepository<Examination, Long>, ExaminationRepositoryCustom {



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
	 * Get a list of examinations for a list of subjects.
	 * 
	 * @param subjectIds
	 * @return list of examinations.
	 */
	List<Examination> findBySubjectIdIn(List<Long> subjectId);
	
	/**
	 * 
	 * @param subjectId:
	 * @param studyId
	 * @return list of examinations.
	 */
	List<Examination> findBySubjectIdAndStudyId(Long subjectId, Long studyId);

	
	/**
	 * Get a paginated list of examinations
	 * 
	 * @param preclinical preclinical examination
	 * @param pageable pagination data.
	 * @return list of examinations.
	 */
	Page<Examination> findAllByPreclinical(Pageable pageable, boolean preclinical);

	/**
	 * Get a list of examinations for a study.
	 * 
	 * @param subjectId
	 * @return
	 * @author yyao
	 *            subject id.
	 * @return list of examinations.
	 */
	List<Examination> findByStudyId(Long studyId);
	
	/**
	 * Get all examinations, clinical or preclinical.
	 * 
	 * @return list of examinations.
	 */
	List<Examination> findAll();
	
	/**
	 * Get all examinations, clinical or preclinical filtered by the patient/subject name.
	 * 
	 * @param patientName
	 * @param pageable
	 * @return
	 */
	Page<Examination> findAllBySubjectName(String patientName, Pageable pageable);

}
