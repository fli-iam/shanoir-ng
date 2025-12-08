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

package org.shanoir.ng.dataset.repository;

import java.util.List;
import java.util.Set;

import org.shanoir.ng.dataset.dto.DatasetForRightsProjection;
import org.shanoir.ng.dataset.dto.DatasetLight;
import org.shanoir.ng.dataset.dto.DatasetStudyCenter;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.tag.model.StudyTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface DatasetRepository extends PagingAndSortingRepository<Dataset, Long>, CrudRepository<Dataset, Long> {

    @Query(value = "SELECT COUNT(*) FROM dataset as ds "
            + "INNER JOIN dataset_acquisition as acq ON ds.dataset_acquisition_id=acq.id "
            + "INNER JOIN examination as ex ON acq.examination_id=ex.id "
            + "WHERE ds.source_id=:datasetParentId AND ex.study_id=:studyId", nativeQuery = true)
    Long countDatasetsBySourceIdAndStudyId(Long datasetParentId, Long studyId);

    Page<Dataset> findByDatasetAcquisitionExaminationStudy_IdIn(Iterable<Long> studyIds, Pageable pageable);

    Iterable<Dataset> findByDatasetAcquisitionExaminationStudy_IdIn(Iterable<Long> studyIds, Sort sort);

    Iterable<Dataset> findByDatasetAcquisition_Examination_Study_Id(Long studyId);

    int countByDatasetAcquisition_Examination_Study_Id(Long studyId);

    @Query(value = "SELECT ds.id FROM dataset ds "
            + "INNER JOIN dataset_acquisition acq ON ds.dataset_acquisition_id = acq.id "
            + "INNER JOIN examination ex ON acq.examination_id = ex.id "
            + "WHERE ex.study_id = ?1", nativeQuery = true)
    List<Long> findIdsByStudyId(Long studyId);

    @Query(value = "SELECT ds.id FROM dataset ds "
            + "WHERE ds.subject_id IN (?1)", nativeQuery = true)
    List<Long> findIdsBySubjectIdIn(List<Long> subjectIds);

    Iterable<Dataset> findByDatasetAcquisitionId(Long acquisitionId);

    Iterable<Dataset> findBydatasetAcquisitionStudyCardId(Long studycardId);

    Iterable<Dataset> findByDatasetAcquisitionStudyCardIdAndDatasetAcquisitionExaminationStudy_IdIn(Long studycardId, List<Long> studyIds);

    Iterable<Dataset> findByDatasetAcquisitionExaminationId(Long examId);

    @Query(value = "SELECT ds.id FROM dataset ds "
            + "LEFT JOIN dataset_acquisition acq ON ds.dataset_acquisition_id = acq.id "
            + "LEFT JOIN dataset_processing processing ON ds.dataset_processing_id = processing.id "
            + "LEFT JOIN input_of_dataset_processing tempo ON tempo.processing_id = processing.id "
            + "LEFT JOIN dataset inputs ON tempo.dataset_id = inputs.id "
            + "LEFT JOIN dataset_acquisition inputAcq ON inputs.dataset_acquisition_id = inputAcq.id "
            + "WHERE acq.examination_id = :examId OR inputAcq.examination_id = :examId", nativeQuery = true)
    List<Long> findDatasetAndOutputByExaminationId(Long examId);


    @Query("SELECT expr.datasetExpressionFormat, SUM(expr.size) FROM DatasetExpression expr "
            + "WHERE expr.dataset.datasetAcquisition.examination.study.id = :studyId AND expr.size IS NOT NULL "
            + "GROUP BY expr.datasetExpressionFormat")
    List<Object[]> findExpressionSizesByStudyIdGroupByFormat(Long studyId);

    @Query("SELECT expr.dataset.datasetAcquisition.examination.study.id, expr.datasetExpressionFormat, SUM(expr.size) FROM DatasetExpression expr "
            + "WHERE expr.dataset.datasetAcquisition.examination.study.id in (:studyIds) AND expr.size IS NOT NULL "
            + "GROUP BY expr.dataset.datasetAcquisition.examination.study.id, expr.datasetExpressionFormat")
    List<Object[]> findExpressionSizesTotalByStudyIdGroupByFormat(List<Long> studyIds);

    List<Dataset> deleteByDatasetProcessingId(Long id);

    boolean existsByTagsContains(StudyTag tag);

    @Query(value = "SELECT ds.id FROM dataset as ds "
            + "INNER JOIN input_of_dataset_processing as input ON ds.id=input.dataset_id "
            + "WHERE input.processing_id in :processingIds or ds.dataset_processing_id in :processingIds", nativeQuery = true)
    List<Dataset> findDatasetsByProcessingIdIn(List<Long> processingIds);

    @Query("""
            SELECT DISTINCT
                ds.id                      AS id,
                ex.study.id                AS studyId,
                ex.centerId                AS centerId,
                relSt.id                   AS relatedStudiesIds
            FROM DatasetProcessing dp
                JOIN dp.inputDatasets ds
                LEFT JOIN ds.datasetAcquisition da
                LEFT JOIN da.examination ex
                LEFT JOIN ds.relatedStudies relSt
            WHERE dp.id IN :processingIds
            """)
    List<DatasetForRightsProjection> findAllInputsByProcessingId(@Param("processingIds") List<Long> processingIds);

    @Query("SELECT new org.shanoir.ng.dataset.dto.DatasetLight( "
            + "ds.id, dm.name, TYPE(ds), "
            + "s.id, s.name, "
            + "sub.id, sub.name, "
            + "ds.creationDate, "
            + "(CASE WHEN EXISTS (SELECT 1 FROM DatasetProcessing p JOIN p.inputDatasets d WHERE d.id = ds.id) THEN true ELSE false END)) "
            + "FROM Dataset ds "
            + "LEFT JOIN ds.originMetadata dm "
            + "LEFT JOIN ds.datasetAcquisition da "
            + "LEFT JOIN da.examination e "
            + "LEFT JOIN e.study s "
            + "LEFT JOIN e.subject sub "
            + "WHERE ds.id IN :ids")
    List<DatasetLight> findAllLightById(List<Long> ids);

    // select rd.study_id from related_datasets rd where dataset_id = ?1
    @Query("""
            SELECT DISTINCT
                      ds.id                      AS id,
                      COALESCE(ex.study.id, dp.studyId) AS studyId,
                      COALESCE(ex.centerId, ex2.centerId) AS centerId,
                      relSt.id                   AS relatedStudiesIds
            FROM Dataset ds
            LEFT JOIN ds.datasetAcquisition da
                  LEFT JOIN da.examination ex
                  LEFT JOIN ds.datasetProcessing dp
                  LEFT JOIN dp.inputDatasets inputDs
                  LEFT JOIN inputDs.datasetAcquisition da2
                  LEFT JOIN da2.examination ex2
            LEFT JOIN ds.relatedStudies relSt
            WHERE ds.id IN :ids
            """)
    List<DatasetForRightsProjection> findDatasetsForRights(@Param("ids") List<Long> datasetIds);

    @Query("SELECT new org.shanoir.ng.dataset.dto.DatasetLight( "
            + "ds.id, dm.name, TYPE(ds), "
            + "s.id, s.name, "
            + "sub.id, sub.name, "
            + "ds.creationDate, "
            + "(CASE WHEN EXISTS (SELECT 1 FROM DatasetProcessing p JOIN p.inputDatasets d WHERE d.id = ds.id) THEN true ELSE false END)) "
            + "FROM Dataset ds "
            + "LEFT JOIN ds.originMetadata dm "
            + "LEFT JOIN ds.datasetAcquisition da "
            + "LEFT JOIN da.examination e "
            + "LEFT JOIN e.study s "
            + "LEFT JOIN e.subject sub "
            + "WHERE s.id = :studyId")
    List<DatasetLight> findAllLightByStudyId(Long studyId);

    @Query("SELECT new org.shanoir.ng.dataset.dto.DatasetStudyCenter("
            + "ds.id, ex.study.id, ex.centerId) "
            + "FROM Dataset ds "
            + "LEFT JOIN ds.datasetAcquisition da "
            + "LEFT JOIN da.examination ex "
            + "WHERE da.id in :acquisitionIds "
            + "OR ex.id in :examinationIds")
    Set<DatasetStudyCenter> getDatasetsByAcquisitionAndExaminationIds(
            @Param("acquisitionIds") List<Long> acquisitionIds,
            @Param("examinationIds") List<Long> examinationIds);

}
