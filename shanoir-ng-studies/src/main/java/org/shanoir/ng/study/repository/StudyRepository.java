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

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends CrudRepository<Study, Long>, StudyRepositoryCustom {
	
	@EntityGraph("Study.All")
	Optional<Study> findById(Long id);

	@Query("SELECT s.name FROM Study s WHERE s.id = :id")
	Optional<String> findNameById(@Param("id") Long id);

	@EntityGraph("Study.All")
	void deleteById(Long id);

	@EntityGraph(attributePaths = { "studyTags", "profile" })
	List<Study> findByVisibleByDefaultTrue();

	//@EntityGraph(attributePaths = { "profile", "tags" })
	List<Study> findAll();

	@Query("SELECT new org.shanoir.ng.shared.core.model.IdName(s.id, s.name) FROM Study s")
	List<IdName> findAllIdAndName();

	@Query("""
		SELECT s 
		FROM Study s 
		JOIN FETCH s.studyUserList su 
		WHERE su.study.id = s.id 
		AND su.userId = :userId and :studyUserRightId in elements(su.studyUserRights) 
		AND su.confirmed = :confirmed""")
	List<Study> findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(
			@Param("userId") Long userId,
			@Param("studyUserRightId") Integer studyUserRightId,
			@Param("confirmed") boolean confirmed
	);

	@Query("""
		SELECT new org.shanoir.ng.shared.core.model.IdName(s.id, s.name)
		FROM Study s
		JOIN s.studyUserList su
		WHERE su.userId = :userId
		AND :studyUserRightId IN elements(su.studyUserRights)
		AND su.confirmed = :confirmed
		ORDER BY s.name ASC""")
	List<IdName> findIdAndNameByUserAndRight(
			@Param("userId") Long userId,
			@Param("studyUserRightId") Integer studyUserRightId,
			@Param("confirmed") boolean confirmed
	);

	List<Study> findByChallengeTrue();

	List<Study> findByStudyUserList_UserIdOrderByNameAsc(Long userId);	

	@EntityGraph(attributePaths = "tags")
    @Query("SELECT s FROM Study s WHERE s.id = :studyId")
    Study findStudyWithTagsById(@Param("studyId") Long studyId);

	@Query("SELECT s.protocolFilePaths FROM Study s WHERE s.id = :studyId")
    List<String> findProtocolFilePathsByStudyId(Long studyId);

	@Query("SELECT s.dataUserAgreementPaths FROM Study s WHERE s.id = :studyId")
    List<String> findDataUserAgreementPathsByStudyId(Long studyId);

}
