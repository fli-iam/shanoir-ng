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

package org.shanoir.ng.subjectstudy.repository;

import java.util.List;

import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.model.SubjectStudyTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for SubjectStudy.
 *
 * @author msimon
 */
public interface SubjectStudyRepository extends CrudRepository<SubjectStudy, Long> {

	@EntityGraph(attributePaths = { "subjectStudyTags.tag.name", "subjectStudyTags.tag.study.name", "subject", "study" })
    @Query("SELECT ss FROM SubjectStudy ss " +
           "LEFT JOIN FETCH ss.subjectStudyTags sst " +
           "LEFT JOIN FETCH sst.tag t " +
           "WHERE ss.study.id = :studyId")
    List<SubjectStudy> findByStudyId(@Param("studyId") Long studyId);

    List<SubjectStudy> findByStudyIdAndStudy_StudyUserList_UserId(Long studyId, Long userId);

	SubjectStudy findByStudyIdAndSubjectId(Long studyId, Long subjectId);

	long countBySubject(Subject subject);
	
    int countByStudyId(@Param("studyId") Long studyId);
    
    @Query("SELECT s.study.id, COUNT(s) FROM SubjectStudy s GROUP BY s.study.id")
    List<Object[]> countByStudyIdGroupBy();

    @Query("SELECT sst FROM SubjectStudyTag sst WHERE sst.subjectStudy.study.id = :studyId and sst.subjectStudy.subject.id = :subjectId")
    List<SubjectStudyTag> findSubjectStudyTagsByStudyIdAndSubjectId(@Param("studyId") Long studyId, @Param("subjectId") Long subjectId);

}
