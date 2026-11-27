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
package org.shanoir.ng.shared.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 * @author yyao
 *
 */
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Subject findByName(String name);

    Subject findByNameAndStudy_Id(String name, Long studyId);

    List<Subject> findByStudy(Study study);

    List<Subject> findByStudy_Id(Long studyId);

    /**
     * Id of the study of a subject, without loading the subject.
     *
     * @param subjectId the subject id
     * @return the study id, empty if the subject does not exist or has no study
     */
    @Query("select s.study.id from Subject s where s.id = :subjectId")
    Optional<Long> findStudyIdById(@Param("subjectId") Long subjectId);

    @Query("select s.id, s.study.id from Subject s where s.id in :subjectIds")
    List<Object[]> findStudyIdRowsByIds(@Param("subjectIds") List<Long> subjectIds);

    /**
     * Id of the study of several subjects, without loading them.
     *
     * @param subjectIds the subject ids
     * @return the study id by subject id, the unknown subjects and the subjects without study being left out
     */
    default Map<Long, Long> findStudyIdsByIds(List<Long> subjectIds) {
        Map<Long, Long> studyIds = new HashMap<>();
        if (subjectIds == null || subjectIds.isEmpty()) {
            return studyIds;
        }
        for (Object[] row : findStudyIdRowsByIds(subjectIds)) {
            studyIds.put((Long) row[0], (Long) row[1]);
        }
        return studyIds;
    }

}
