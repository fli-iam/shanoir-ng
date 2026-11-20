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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {

    @EntityGraph("Study.All")
    Optional<Study> findById(Long id);

    @Query("SELECT s.name FROM Study s WHERE s.id = :id")
    Optional<String> findNameById(@Param("id") Long id);

    @EntityGraph("Study.All")
    void deleteById(Long id);

    @EntityGraph(attributePaths = { "studyTags", "profile" })
    List<Study> findByVisibleByDefaultTrueAndIsDraftFalse();

    /**
     * Studies exposed to the Neurobagel federation: flagged by an admin and no longer drafts.
     */
    List<Study> findByNeurobagelExportTrueAndIsDraftFalseOrderByIdAsc();

    boolean existsByIdAndNeurobagelExportTrueAndIsDraftFalse(Long id);

    @EntityGraph(attributePaths = { "studyTags", "profile" })
    List<Study> findDistinctByStudyUserListUserIdAndStudyUserListExpirationDateBefore(Long userId, LocalDate date);

    List<Study> findByIsDraftTrue();

    List<Study> findAll();

    @Query(value = "SELECT id from study", nativeQuery = true)
    List<Long> findAllIds();

    @Query("SELECT new org.shanoir.ng.shared.core.model.IdName(s.id, s.name) FROM Study s WHERE :challenge IS NOT TRUE OR s.challenge IS TRUE")
    List<IdName> findAllIdAndName(Boolean challenge);

    @Query("""
        SELECT DISTINCT s.id
        FROM Study s
        WHERE EXISTS (
            SELECT su FROM StudyUser su
            WHERE su.study = s
            AND su.userId = :userId AND :studyUserRightId IN elements(su.studyUserRights)
            AND su.confirmed = :confirmed)
        ORDER BY s.name ASC""")
    List<Long> findIdsByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(
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
        AND (:challenge IS NOT TRUE OR s.challenge IS TRUE)
        ORDER BY s.name ASC""")
    List<IdName> findIdAndNameByUserAndRight(
            Long userId,
            Integer studyUserRightId,
            boolean confirmed,
            Boolean challenge
    );

    List<Study> findByChallengeTrue();


    List<Study> findByStudyUserList_UserIdOrderByNameAsc(Long userId);

    @EntityGraph(attributePaths = "tags")
    @Query("SELECT s FROM Study s WHERE s.id = :studyId")
    Study findStudyWithTagsById(@Param("studyId") Long studyId);

    /**
     * The study with its own tags, the keywords of its Neurobagel dataset description.
     */
    @EntityGraph(attributePaths = "studyTags")
    @Query("SELECT s FROM Study s WHERE s.id = :studyId")
    Study findStudyWithStudyTagsById(@Param("studyId") Long studyId);

    @Query("SELECT s.id, p FROM Study s JOIN s.protocolFilePaths p WHERE s.id IN :studyIds")
    List<Object[]> findProtocolFilePathsByStudyIds(@Param("studyIds") List<Long> studyIds);

    @Query("SELECT s.id, p FROM Study s JOIN s.dataUserAgreementPaths p WHERE s.id IN :studyIds")
    List<Object[]> findDataUserAgreementPathsByStudyIds(@Param("studyIds") List<Long> studyIds);

    @Query("SELECT su.study.id FROM StudyUser su WHERE su.userId = :userId AND :right MEMBER OF su.studyUserRights")
    List<Long> findByUserIdAndStudyUserRight(Long userId, Integer right);

    @Query("SELECT distinct s.study.id from Subject s where s.id in :subjectIds")
    List<Long> findStudyIdsBySubjectIds(List<Long> subjectIds);

    @Query("SELECT s.isDraft FROM Study s WHERE s.id = :studyId")
    Optional<Boolean> findIsDraftById(@Param("studyId") Long id);

    @Transactional
    default List<Study> findAllIdAndNameAndCenterListFromIds(List<Long> ids, Boolean withCenterList) {
        List<Study> studies = findAllById(ids);
        if (withCenterList) {
            studies.forEach(s -> Hibernate.initialize(s.getStudyCenterList()));
        }
        return studies;
    }
}
