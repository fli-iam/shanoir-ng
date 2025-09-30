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

package org.shanoir.ng.studyexamination;

import java.util.List;

import org.shanoir.ng.subject.model.Subject;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for relations between a study and a center.
 *
 * @author msimon
 */
public interface StudyExaminationRepository extends CrudRepository<StudyExamination, Long> {

	public Iterable<StudyExamination> findByCenterId(Long centerId);

    public void deleteBySubject(Subject subject);

    @Modifying
    @Query("DELETE FROM StudyExamination se WHERE se.subject.id = :subjectId")
    public void deleteBySubjectId(Long subjectId);

    int countByStudyId(@Param("studyId") Long studyId);

    @Query("SELECT s.study.id, COUNT(s) FROM StudyExamination s GROUP BY s.study")
    List<Object[]> countByStudyIdGroupBy();

}
