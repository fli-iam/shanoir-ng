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

package org.shanoir.ng.study.rights;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for relations between a study and an user.
 */
public interface StudyUserRightsRepository extends CrudRepository<StudyUser, Long> {

	@Transactional
	void deleteByIdIn(Set<Long> ids);

	StudyUser findByUserIdAndStudyId(Long userId, Long studyId);

	Iterable<StudyUser> findByUserIdAndStudyIdIn(Long userId, Set<Long> studyIds);

	Iterable<StudyUser> findByUserId(Long userId);
	
	Iterable<StudyUser> findByStudyId(Long studyId);

	@Query("select su.studyId from StudyUser su where su.userId = :userId and :right in elements(su.studyUserRights)")
	List<Long> findDistinctStudyIdByUserId(@Param("userId") Long userId, @Param("right") int right);
}
