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
	 * Get a paginated list of examinations for a list of studies filtered by subject name.
	 * 
	 * @param studyIds
	 * @param patientName
	 * @param pageable
	 * @return
	 */
	@Query(value = "SELECT * FROM examination AS ex JOIN subject AS sub WHERE ex.subject_id = sub.id AND ex.study_id in (?1) AND sub.name like %?2%",
			countQuery = "SELECT count(*) FROM examination AS ex JOIN subject AS sub WHERE ex.subject_id = sub.id AND ex.study_id in (?1) AND sub.name like %?2%",
			nativeQuery = true)
	Page<Examination> findByStudyIdInAndBySubjectName(List<Long> studyIds, String patientName, Pageable pageable);
	
	/**
	 * Get all examinations for a list of studies.
	 * 
	 * @param studyIds
	 * @return
	 */
	List<Examination> findByStudyIdIn(List<Long> studyIds);

	@Query(value = "SELECT * FROM examination e "
			+ "LEFT JOIN study_user su "
			+ "ON su.study_id = e.study_id "
			+ "LEFT JOIN study_user_center suc "
			+ "ON suc.study_user_id = su.id "
			+ "AND suc.centers_ids = e.center_id "
			+ "AND e.study_id in :studyIds",
			    countQuery = "SELECT count(*) FROM examination e "
						+ "LEFT JOIN study_user su "
						+ "ON su.study_id = e.study_id "
						+ "LEFT JOIN study_user_center suc "
						+ "ON suc.study_user_id = su.id "
						+ "AND suc.centers_ids = e.center_id "
						+ "AND e.study_id in :studyIds",
			    nativeQuery = true)
	Page<Examination>findByStudyIdInFilterByCenter(@Param("studyIds") List<Long> studyIds, Pageable pageable);
	
	/**
	 * Get a paginated list of examinations for a list of studies.
	 * 
	 * @param studyIds
	 *            list of study ids.
	 * @param sort
	 *            pagination data.
	 * @return list of examinations.
	 */
	@Query(value = "SELECT * FROM examination e "
			+ "LEFT JOIN study_user su "
			+ "ON su.study_id = e.study_id "
			+ "LEFT JOIN study_user_center suc "
			+ "ON suc.study_user_id = su.id "
			+ "AND suc.centers_ids = e.center_id "
			+ "AND e.study_id in :studyIds "
			+ "AND e.preclinical = :preclinical",
			    countQuery ="SELECT count(*) FROM examination e "
						+ "LEFT JOIN study_user su "
						+ "ON su.study_id = e.study_id "
						+ "LEFT JOIN study_user_center suc "
						+ "ON suc.study_user_id = su.id "
						+ "AND suc.centers_ids = e.center_id "
						+ "AND e.study_id in :studyIds "
						+ "AND e.preclinical = :preclinical",
			    nativeQuery = true)
	Page<Examination>findByPreclinicalAndStudyIdInFilterByCenter(@Param("preclinical")Boolean preclinical, @Param("studyIds") List<Long> studyIds, Pageable pageable);

	Page<Examination> findByPreclinicalAndStudyIdIn(Boolean preclinical, List<Long> studyIds, Pageable page);

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
	 * Get a paginated list of examinations for a list of studies.
	 * 
	 * @param studyIds list of study ids.
	 * @param preclinical preclinical examination
	 * @param pageable pagination data.
	 * @return list of examinations.
	 */
	Page<Examination> findByStudyIdInAndPreclinical(List<Long> studyIds, boolean preclinical, Pageable pageable);
	
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
	@Query(value = "SELECT * FROM examination AS ex JOIN subject AS sub WHERE ex.subject_id = sub.id AND sub.name like %?1%",
			countQuery = "SELECT count(*) FROM examination AS ex JOIN subject AS sub WHERE ex.subject_id = sub.id AND sub.name like %?1%",
			nativeQuery = true)
	Page<Examination> findAllBySubjectName(String patientName, Pageable pageable);

}
