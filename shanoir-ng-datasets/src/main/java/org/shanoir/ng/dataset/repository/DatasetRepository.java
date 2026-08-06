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
import java.util.Optional;
import java.util.Set;

import org.hibernate.Hibernate;
import org.shanoir.ng.dataset.dto.DatasetForRightsProjection;
import org.shanoir.ng.dataset.dto.DatasetLight;
import org.shanoir.ng.dataset.dto.DatasetStudyCenter;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetRightsDTO;
import org.shanoir.ng.dataset.model.OverallStatistics;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.tag.model.StudyTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DatasetRepository extends PagingAndSortingRepository<Dataset, Long>, JpaRepository<Dataset, Long> {

    @Query(value = "SELECT COUNT(*) FROM dataset as ds "
            + "INNER JOIN dataset_acquisition as acq ON ds.dataset_acquisition_id=acq.id "
            + "INNER JOIN examination as ex ON acq.examination_id=ex.id "
            + "WHERE ds.source_id=:datasetParentId AND ex.study_id=:studyId", nativeQuery = true)
    Long countDatasetsBySourceIdAndStudyId(Long datasetParentId, Long studyId);

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

    List<Dataset> findByDatasetAcquisitionId(Long acquisitionId);

    int countByDatasetAcquisitionId(Long acquisitionId);

    @Query(value = "SELECT ds.id FROM dataset ds "
            + "LEFT JOIN dataset_acquisition acq ON ds.dataset_acquisition_id = acq.id "
            + "WHERE acq.examination_id = :examId "
            + "UNION "
            + "SELECT ds.id FROM dataset ds "
            + "LEFT JOIN dataset_processing processing ON ds.dataset_processing_id = processing.id "
            + "LEFT JOIN input_of_dataset_processing tempo ON tempo.processing_id = processing.id "
            + "LEFT JOIN dataset inputs ON tempo.dataset_id = inputs.id "
            + "LEFT JOIN dataset_acquisition inputAcq ON inputs.dataset_acquisition_id = inputAcq.id "
            + "WHERE inputAcq.examination_id = :examId", nativeQuery = true)
    List<Long> findDatasetAndOutputByExaminationId(Long examId);

    @Query("SELECT expr.datasetExpressionFormat, SUM(expr.size) FROM DatasetExpression expr "
            + "WHERE expr.dataset.datasetAcquisition.examination.study.id = :studyId AND expr.size IS NOT NULL "
            + "GROUP BY expr.datasetExpressionFormat")
    List<Object[]> findExpressionSizesByStudyIdGroupByFormat(Long studyId);

    @Query("SELECT expr.dataset.datasetAcquisition.examination.study.id, expr.datasetExpressionFormat, SUM(expr.size) FROM DatasetExpression expr "
            + "WHERE expr.dataset.datasetAcquisition.examination.study.id in (:studyIds) AND expr.size IS NOT NULL "
            + "GROUP BY expr.dataset.datasetAcquisition.examination.study.id, expr.datasetExpressionFormat")
    List<Object[]> findExpressionSizesTotalByStudyIdGroupByFormat(List<Long> studyIds);

    @Query("SELECT SUM(expr.size) FROM DatasetExpression expr WHERE expr.size IS NOT NULL")
    Long findDatasetsExpressionSizesSum();

    boolean existsByTagsContains(StudyTag tag);

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
            + "COALESCE(s.id, s2.id), COALESCE(s.name, s2.name), "
            + "COALESCE(sub.id, sub2.id), COALESCE(sub.name, sub2.name), "
            + "ds.creationDate, "
            + "(CASE WHEN EXISTS (SELECT 1 FROM DatasetProcessing p JOIN p.inputDatasets d WHERE d.id = ds.id) THEN true ELSE false END), "
            + "e.centerId) "
            + "FROM Dataset ds "
            + "LEFT JOIN ds.originMetadata dm "
            + "LEFT JOIN ds.datasetAcquisition da "
            + "LEFT JOIN da.examination e "
            + "LEFT JOIN e.study s "
            + "LEFT JOIN e.subject sub "
            + "LEFT JOIN Subject sub2 ON sub2.id = ds.subjectId "
            + "LEFT JOIN sub2.study s2 "
            + "WHERE ds.id IN :ids ")
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
            + "(CASE WHEN EXISTS (SELECT 1 FROM DatasetProcessing p JOIN p.inputDatasets d WHERE d.id = ds.id) THEN true ELSE false END), "
            + "e.centerId) "
            + "FROM Dataset ds "
            + "LEFT JOIN ds.originMetadata dm "
            + "LEFT JOIN ds.datasetAcquisition da "
            + "LEFT JOIN da.examination e "
            + "LEFT JOIN e.study s "
            + "LEFT JOIN e.subject sub "
            + "WHERE s.id = :studyId")
    List<DatasetLight> findAllLightByStudyId(Long studyId);

    @Transactional
    @Modifying
    @Query(value = "CALL computeOverallStatistics()", nativeQuery = true)
    void computeOverallStatistics();

    @Modifying
    @Transactional
    @Query(value = "UPDATE overall_statistics os SET os.storage_size = :totalStorageVolume WHERE os.stats_date = CURDATE()", nativeQuery = true)
    void addTotalStorageVolume(@Param("totalStorageVolume") Long totalStorageVolume);

    @Query(value = "SELECT os from OverallStatistics os WHERE os.statsDate = (SELECT MAX(os2.statsDate) FROM OverallStatistics os2)")
    List<OverallStatistics> getOverallStatistics();

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

    @Query("""
            SELECT new org.shanoir.ng.dataset.model.DatasetRightsDTO(
                d.id,
                e.centerId,
                dp.studyId,
                e.study.id
            )
            FROM Dataset d
            LEFT JOIN d.datasetProcessing dp
            LEFT JOIN d.datasetAcquisition da
            LEFT JOIN da.examination e
            WHERE d.id = :id
            """)
    DatasetRightsDTO findRightsDtoBaseById(@Param("id") Long id);

    @Query("""
            SELECT rs.id FROM Dataset d JOIN d.relatedStudies rs WHERE d.id = :id
            """)
    Set<Long> findRelatedStudyIds(@Param("id") Long id);

    @Query(value = "SELECT ds.id FROM dataset ds "
            + "JOIN dataset_metadata AS meta ON ds.updated_metadata_id = meta.id "
            + "WHERE ds.dataset_acquisition_id IN (?1) "
            + "AND (?2 = '' OR meta.name LIKE ?2)", nativeQuery = true)
    List<Long> findFilteredIdsByDatasetAcquisitionIdIn(List<Long> acquisitionIds, String filter);

    @Query(value = "SELECT ds.id FROM dataset ds "
            + "JOIN dataset_metadata AS meta ON ds.updated_metadata_id = meta.id "
            + "WHERE ds.dataset_acquisition_id = ?1 "
            + "AND (?2 = '' OR meta.name LIKE ?2)", nativeQuery = true)
    List<Long> findFilteredIdsByDatasetAcquisitionId(Long acquisitionId, String filter);

    @Query(value = "SELECT dataset.id FROM dataset "
            + "JOIN dataset_acquisition acq ON acq.id = dataset.dataset_acquisition_id "
            + "WHERE acq.examination_id = ?1", nativeQuery = true)
    List<Long> findIdsByExaminationId(Long examinationId);

    @Query("SELECT DISTINCT de.dataset.id FROM DatasetExpression de WHERE de.dataset.id IN :ids")
    Set<Long> findDatasetIdsHavingExpressions(List<Long> ids);

    @Query("SELECT dataset FROM Dataset dataset "
            + "JOIN FETCH dataset.datasetProcessing AS dp "
            + "JOIN FETCH dp.inputDatasets "
            + "WHERE dataset.id = :id")
    Dataset findByIdWithProcessingAncestors(Long id);

    @Query("SELECT dataset FROM Dataset dataset "
            + "JOIN FETCH dataset.datasetProcessing AS dp "
            + "JOIN FETCH dp.inputDatasets "
            + "WHERE dataset.id in :ids")
    List<Dataset> findByIdsWithProcessingAncestors(List<Long> ids);

    @Query("SELECT dataset FROM Dataset dataset "
            + "JOIN FETCH dataset.relatedStudies "
            + "JOIN FETCH dataset.datasetProcessing "
            + "JOIN FETCH dataset.datasetAcquisition AS acq "
            + "JOIN FETCH acq.examination "
            + "WHERE dataset.id = :id")
    Dataset findByIdWithExaminationRelationsAndRelatedStudies(Long id);

    @Query("SELECT dataset FROM Dataset dataset "
            + "LEFT JOIN FETCH dataset.datasetProcessing "
            + "WHERE dataset.subjectId = :subjectId")
    Dataset findByIdWithDatasetProcessing(Long id);

    @Query("SELECT dataset FROM Dataset dataset "
            + "LEFT JOIN FETCH dataset.tags "
            + "WHERE dataset.subjectId = :subjectId")
    Dataset findByIdWithTags(Long id);

    boolean existsBySourceId(Long sourceId);

    @Query("SELECT dataset FROM Dataset dataset "
            + "WHERE dataset.subjectId = :subjectId")
    List<Dataset> findBySubjectId(Long subjectId);

    @Query("SELECT dataset FROM Dataset dataset "
            + "LEFT JOIN FETCH dataset.datasetProcessing AS dp "
            + "LEFT JOIN FETCH dp.inputDatasets "
            + "LEFT JOIN FETCH dataset.datasetAcquisition AS acq "
            + "LEFT JOIN FETCH acq.examination "
            + "LEFT JOIN FETCH dataset.originMetadata "
            + "LEFT JOIN FETCH dataset.updatedMetadata "
            + "WHERE dataset.id = :id")
    Optional<Dataset> findByIdWithProcessingAncestorsAndExaminationAndMetadata(Long id);

    @Query("SELECT dataset FROM Dataset dataset "
            + "JOIN FETCH dataset.datasetProcessing AS dp "
            + "JOIN FETCH dp.inputDatasets "
            + "JOIN FETCH dataset.datasetAcquisition AS acq "
            + "JOIN FETCH acq.examination "
            + "WHERE dataset.id in :ids")
    List<Dataset> findByIdsWithProcessingAncestorsAndExamination(List<Long> ids);

    @Query("SELECT dataset FROM Dataset dataset "
            + "LEFT JOIN FETCH dataset.datasetProcessing AS dp "
            + "LEFT JOIN FETCH dp.inputDatasets "
            + "LEFT JOIN FETCH dataset.datasetAcquisition AS acq "
            + "LEFT JOIN FETCH acq.examination e "
            + "WHERE acq.id = :acquisitionId")
    List<Dataset> findByAcquisitionIdWithProcessingAncestorsAndExamination(Long acquisitionId);

    @Query("SELECT dataset FROM Dataset dataset "
            + "JOIN FETCH dataset.datasetProcessing AS dp "
            + "JOIN FETCH dp.inputDatasets "
            + "JOIN FETCH dataset.datasetAcquisition AS acq "
            + "JOIN FETCH acq.examination e "
            + "WHERE acq.id = :studyCardId")
    List<Dataset> findByStudyCardIdWithProcessingAncestorsAndExamination(Long studyCardId);

    @Query("SELECT dataset FROM Dataset dataset "
            + "JOIN FETCH dataset.datasetProcessing AS dp "
            + "JOIN FETCH dp.inputDatasets "
            + "JOIN FETCH dataset.datasetAcquisition AS acq "
            + "JOIN FETCH acq.examination e "
            + "WHERE acq.id = :studyCardId "
            + "AND e.study.id IN :studyIds")
    List<Dataset> findByStudyCardIdAndStudyIdsWithProcessingAncestorsAndExamination(Long studyCardId, List<Long> studyIds);

    @Query("SELECT dataset FROM Dataset dataset "
            + "LEFT JOIN FETCH dataset.datasetProcessing AS dp "
            + "LEFT JOIN FETCH dp.inputDatasets "
            + "LEFT JOIN FETCH dataset.datasetAcquisition AS acq "
            + "LEFT JOIN FETCH acq.examination e "
            + "WHERE e.id = :examinationId")
    List<Dataset> findByExaminationIdWithProcessingAncestorsAndExamination(Long examinationId);

    @Query("SELECT dataset FROM Dataset dataset "
            + "JOIN FETCH dataset.datasetProcessing AS dp "
            + "JOIN FETCH dp.inputDatasets "
            + "JOIN FETCH dataset.datasetAcquisition AS acq "
            + "JOIN FETCH acq.examination e "
            + "WHERE e.study.id IN :study_ids "
            + "ANd dataset.id IN :ids")
    List<Dataset> findByIdsAndStudyIdsWithProcessingAncestorsAndExamination(List<Long> studyIds, List<Long> ids);

    @Query(value = "SELECT d FROM Dataset AS d "
            + "LEFT JOIN FETCH d.datasetExpressions de "
            + "LEFT JOIN FETCH d.datasetAcquisition AS acq "
            + "LEFT JOIN FETCH acq.examination e "
            + "LEFT JOIN FETCH d.originMetadata "
            + "LEFT JOIN FETCH d.updatedMetadata "
            + "WHERE d.id = :id")
    Optional<Dataset> findByIdWithDatasetExpressionAndExaminationAndMetadata(Long id);

    @Query(value = "SELECT d FROM Dataset AS d "
            + "LEFT JOIN FETCH d.datasetExpressions de "
            + "LEFT JOIN FETCH d.datasetAcquisition AS acq "
            + "LEFT JOIN FETCH acq.examination e "
            + "LEFT JOIN FETCH d.originMetadata "
            + "LEFT JOIN FETCH d.updatedMetadata "
            + "WHERE acq.id = :id")
    List<Dataset> findByAcquisitionIdWithDatasetExpressionAndExaminationAndMetadata(Long id);

    @Query(value = "SELECT d FROM Dataset AS d "
            + "LEFT JOIN FETCH d.datasetExpressions de "
            + "LEFT JOIN FETCH d.datasetAcquisition AS acq "
            + "LEFT JOIN FETCH acq.examination e "
            + "LEFT JOIN FETCH d.originMetadata "
            + "LEFT JOIN FETCH d.updatedMetadata "
            + "WHERE e.study.id = :id")
    List<Dataset> findByStudyIdWithDatasetExpressionAndExaminationAndMetadata(Long id);

    @Query(value = "SELECT d FROM Dataset AS d "
            + "LEFT JOIN FETCH d.datasetExpressions de "
            + "LEFT JOIN FETCH d.datasetAcquisition AS acq "
            + "LEFT JOIN FETCH acq.examination e "
            + "LEFT JOIN FETCH d.originMetadata "
            + "LEFT JOIN FETCH d.updatedMetadata "
            + "WHERE e.id = :id")
    List<Dataset> findByExaminationIdWithDatasetExpressionAndExaminationAndMetadata(Long id);

    @Query(value = "SELECT d FROM Dataset AS d "
            + "LEFT JOIN FETCH d.datasetExpressions "
            + "WHERE d.id IN :idList")
    List<Dataset> findByIdsWithDatasetExpression(List<Long> idList);

    @Query(value = "SELECT d FROM Dataset AS d "
            + "LEFT JOIN FETCH d.datasetExpressions "
            + "JOIN ProcessingResource AS p ON d.id = p.dataset.id "
            + "WHERE p.resourceId = :resourceId")
    List<Dataset> findByResourceIdWithDatasetExpression(String resourceId);

    @Query(value = "SELECT d FROM Dataset AS d "
            + "JOIN ProcessingResource AS p ON d.id = p.dataset.id "
            + "WHERE p.resourceId = :resourceId")
    List<Dataset> findByResourceId(String resourceId);

    @Query(value = "SELECT d.id FROM Dataset d",
            countQuery = "SELECT COUNT(d) FROM Dataset d")
    Page<Long> findAllIds(Pageable pageable);

    @Query(value = "SELECT d.id FROM Dataset d")
    List<Long> findAllIds(Sort sort);

    default Page<Dataset> findAllWithProcessingAncestorsAndExamination(Pageable pageable) {
        Page<Long> idPage = findAllIds(pageable);
        List<Dataset> datasetList = findByIdsWithProcessingAncestorsAndExamination(idPage.getContent());
        return new PageImpl<>(datasetList, pageable, idPage.getTotalElements());
    }

    default Page<Dataset> findByStudyIdsWithProcessingAncestorsAndExamination(List<Long> studyIds, Pageable pageable) {
        Page<Long> idPage = findAllIds(pageable);
        List<Dataset> datasetList = findByIdsAndStudyIdsWithProcessingAncestorsAndExamination(idPage.getContent(), studyIds);
        return new PageImpl<>(datasetList, pageable, idPage.getTotalElements());
    }

    default List<Dataset> findByStudyIdsWithProcessingAncestorsAndExamination(List<Long> studyIds, Sort sort) {
        return findByIdsAndStudyIdsWithProcessingAncestorsAndExamination(findAllIds(sort), studyIds);
    }

    @Transactional(readOnly = true)
    default List<Dataset> findByAcquisitionIdWithDatasetFilesAndExaminationAndMetadata(Long acquisitionId) {
        List<Dataset> datasets = findByAcquisitionIdWithDatasetExpressionAndExaminationAndMetadata(acquisitionId);
        datasets.forEach(d ->
                d.getDatasetExpressions().forEach(de ->
                    Hibernate.initialize(de.getDatasetFiles())
                )
        );
        return datasets;
    }

    @Transactional(readOnly = true)
    default List<Dataset> findByExaminationIdWithDatasetFilesAndExaminationAndMetadata(Long examinationId) {
        List<Dataset> datasets = findByExaminationIdWithDatasetExpressionAndExaminationAndMetadata(examinationId);
        datasets.forEach(d ->
                d.getDatasetExpressions().forEach(de -> {
                    Hibernate.initialize(de.getDatasetFiles());
                })
        );
        return datasets;
    }

    @Transactional(readOnly = true)
    default List<Dataset> findByStudyIdWithDatasetFilesAndExaminationAndMetadata(Long studyId) {
        List<Dataset> datasets = findByStudyIdWithDatasetExpressionAndExaminationAndMetadata(studyId);
        datasets.forEach(d ->
                d.getDatasetExpressions().forEach(de -> {
                    Hibernate.initialize(de.getDatasetFiles());
                })
        );
        return datasets;
    }

    @Transactional(readOnly = true)
    default Optional<Dataset> findByIdWithDatasetFilesAndExaminationAndMetadata(Long id) {
        Optional<Dataset> dataset = findByIdWithDatasetExpressionAndExaminationAndMetadata(id);
        dataset.ifPresent(d ->
                d.getDatasetExpressions().forEach(de -> {
                    Hibernate.initialize(de.getDatasetFiles());
                })
        );
        return dataset;
    }

    @Transactional(readOnly = true)
    default List<Dataset> findByIdsWithDatasetFilesAndExaminationAndMetadata(List<Long> ids) {
        List<Dataset> datasets = findByIdsWithDatasetExpression(ids);
        datasets.forEach(d ->
                d.getDatasetExpressions().forEach(de -> {
                    Hibernate.initialize(de.getDatasetFiles());
                })
        );
        return datasets;
    }

    @Transactional(readOnly = true)
    default List<Dataset> findByResourceIdWithDatasetFiles(String id) {
        List<Dataset> datasets = findByResourceIdWithDatasetExpression(id);
        datasets.forEach(d ->
                d.getDatasetExpressions().forEach(de -> {
                    Hibernate.initialize(de.getDatasetFiles());
                })
        );
        return datasets;
    }
}
