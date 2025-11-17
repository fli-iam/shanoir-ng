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
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

/**
 * Repository for relations between a study and an user.
 */
public interface StudyUserRightsRepository extends CrudRepository<StudyUser, Long> {

	@Transactional
	void deleteByIdIn(Set<Long> ids);

	@EntityGraph(attributePaths = "studyUserRights")
	StudyUser findByUserIdAndStudyId(Long userId, Long studyId);

	@EntityGraph(attributePaths = "studyUserRights")
	Optional<List<StudyUser>> findAllByUserId(Long userId);

	@Query("SELECT su.centerIds FROM StudyUser su WHERE su.id = :id")
    List<Long> findCenterIdsByStudyUserId(@Param("id") Long studyUserId);

	@EntityGraph(attributePaths = "studyUserRights")
	Iterable<StudyUser> findByUserIdAndStudyIdIn(Long userId, Set<Long> studyIds);

	@EntityGraph(attributePaths = "studyUserRights")
	Iterable<StudyUser> findByUserId(Long userId);
	
	Iterable<StudyUser> findByStudyId(Long studyId);
	
	@Query("select su.studyId from StudyUser su where su.userId = :userId and :right in elements(su.studyUserRights)")
	List<Long> findDistinctStudyIdByUserId(Long userId, int right);

	@Query("select su from StudyUser su where su.userId = :userId and :right in elements(su.studyUserRights)")
	Iterable<StudyUser> findByUserIdAndRight(Long userId, int right);
	
	@Query("select su from StudyUser su where su.studyId = :studyId and :right in elements(su.studyUserRights)")
	Iterable<StudyUser> findByStudyIdAndRight(Long studyId, int right);
	
}
