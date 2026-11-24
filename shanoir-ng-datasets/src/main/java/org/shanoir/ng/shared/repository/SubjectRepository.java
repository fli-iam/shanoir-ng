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

import java.util.List;
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

}
