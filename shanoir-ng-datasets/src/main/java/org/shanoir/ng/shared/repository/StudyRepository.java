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

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.model.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yyao
 *
 */
public interface StudyRepository extends JpaRepository<Study, Long> {

    @Query("SELECT study FROM Study study LEFT JOIN FETCH study.studyTags WHERE study.id = :id")
    Optional<Study> findByIdWithStudyTags(Long id);

    @Query("SELECT study FROM Study study LEFT JOIN FETCH study.examinations e WHERE study.id = :id")
    Optional<Study> findByIdWithExaminations(Long id);

    @Query("SELECT DISTINCT e FROM Examination e LEFT JOIN FETCH e.datasetAcquisitions WHERE e.study.id = :id")
    List<Examination> fetchAcquisitionsByStudyId(Long id);

    @Query("SELECT DISTINCT a FROM DatasetAcquisition a LEFT JOIN FETCH a.datasets WHERE a.examination.study.id = :id")
    List<DatasetAcquisition> fetchDatasetsByStudyId(Long id);

    @Query("SELECT DISTINCT d FROM Dataset d LEFT JOIN FETCH d.datasetExpressions WHERE d.datasetAcquisition.examination.study.id = :id")
    List<Dataset> fetchDatasetExpressionsByStudyId(Long id);

    @Query("SELECT DISTINCT de FROM DatasetExpression de LEFT JOIN FETCH de.datasetFiles WHERE de.dataset.datasetAcquisition.examination.study.id = :id")
    List<DatasetExpression> fetchDatasetFilesByStudyId(Long id);

    @Transactional
    default Optional<Study> findByIdWithAcquisitions(Long id) {
        Optional<Study> study = findByIdWithExaminations(id);
        study.ifPresent(s -> fetchAcquisitionsByStudyId(id));
        return study;
    }

    @Transactional
    default Optional<Study> findByIdWithDatasetsAndDatasetFilePaths(Long id) {
        Optional<Study> study = findByIdWithAcquisitions(id);
        study.ifPresent(s -> {
            fetchDatasetsByStudyId(id);
            fetchDatasetExpressionsByStudyId(id);
            fetchDatasetFilesByStudyId(id);
        });
        return study;
    }
}
