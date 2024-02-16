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
import java.util.Optional;

import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.tag.model.Tag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends CrudRepository<Study, Long> {
	
	@EntityGraph("Study.All")
	Optional<Study> findById(Long id);

	@EntityGraph(attributePaths = { "studyTags" })
	List<Study> findByVisibleByDefaultTrue();

	@EntityGraph(attributePaths = { "profile", "tags" })
	List<Study> findAll();
	
	@EntityGraph(attributePaths = { "profile", "tags" })
	List<Study> findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(Long userId, Integer studyUserRightId, boolean confirmed);

	List<Study> findByChallengeTrue();

	List<Study> findByStudyUserList_UserIdOrderByNameAsc(Long userId);	

	@Query("SELECT t FROM Study s LEFT JOIN s.tags t WHERE s.id = :studyId")
    List<Tag> findTagsByStudyId(@Param("studyId") Long studyId);

}
