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

package org.shanoir.ng.study.repository;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.shanoir.ng.study.model.StudyUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for relations between a study and an user.
 *
 * @author msimon
 */
public interface StudyUserRepository extends CrudRepository<StudyUser, Long> {

	List<StudyUser> findByUserId(Long userId);
	
	List<StudyUser> findByStudy_Id(Long studyId);

	StudyUser findByUserIdAndStudy_Id(Long userId, Long studyId);

	@Transactional
	void deleteByIdIn(Set<Long> ids);
	
	@Query("select s.id from StudyUser su inner join su.study as s where su.userId = :userId and su.confirmed = true and :right in elements(su.studyUserRights)")
	List<Long> findDistinctStudyIdByUserId(@Param("userId") Long userId, @Param("right") int right);
}
