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

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.tag.model.StudyTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DatasetRepository extends PagingAndSortingRepository<Dataset, Long>, CrudRepository<Dataset, Long> {

	@Query(value="SELECT COUNT(*) FROM dataset as ds " +
			"INNER JOIN dataset_acquisition as acq ON ds.dataset_acquisition_id=acq.id " +
			"INNER JOIN examination as ex ON acq.examination_id=ex.id " +
			"WHERE ds.source_id=:datasetParentId AND ex.study_id=:studyId", nativeQuery = true)
	Long countDatasetsBySourceIdAndStudyId(Long datasetParentId, Long studyId);

	List<Dataset> findBySourceId(Long sourceDatasetId);

	List<Dataset> findBySourceIdIn(List<Long> sourceDatasetId);

	Page<Dataset> findByDatasetAcquisitionExaminationStudy_IdIn(Iterable<Long> studyIds, Pageable pageable);

	Iterable<Dataset> findByDatasetAcquisitionExaminationStudy_IdIn(Iterable<Long> studyIds, Sort sort);

	Iterable<Dataset> findByDatasetAcquisition_Examination_Study_Id(Long studyId);

	int countByDatasetAcquisition_Examination_Study_Id(Long studyId);

	@Query(value = "SELECT ds.id FROM dataset ds " +
			"INNER JOIN dataset_acquisition acq ON ds.dataset_acquisition_id = acq.id " +
			"INNER JOIN examination ex ON acq.examination_id = ex.id " +
			"WHERE ex.study_id = ?1", nativeQuery = true)
	List<Long> findIdsByStudyId(Long studyId);

	@Query(value = "SELECT ds.id FROM dataset ds " +
			"WHERE ds.subject_id IN (?1)", nativeQuery = true)
	List<Long> findIdsBySubjectIdIn(List<Long> subjectIds);

	Iterable<Dataset> findByDatasetAcquisitionId(Long acquisitionId);
	
	Iterable<Dataset> findBydatasetAcquisitionStudyCardId(Long studycardId);

	Iterable<Dataset> findByDatasetAcquisitionStudyCardIdAndDatasetAcquisitionExaminationStudy_IdIn(Long studycardId, List<Long> studyIds);

	void deleteByIdIn(List<Long> ids);

	Iterable<Dataset> findByDatasetAcquisitionExaminationId(Long examId);

	@Query("SELECT expr.datasetExpressionFormat, SUM(expr.size) FROM DatasetExpression expr " +
			"WHERE expr.dataset.datasetAcquisition.examination.study.id = :studyId AND expr.size IS NOT NULL " +
			"GROUP BY expr.datasetExpressionFormat")
	List<Object[]> findExpressionSizesByStudyIdGroupByFormat(Long studyId);

	@Query("SELECT expr.dataset.datasetAcquisition.examination.study.id, expr.datasetExpressionFormat, SUM(expr.size) FROM DatasetExpression expr " +
			"WHERE expr.dataset.datasetAcquisition.examination.study.id in (:studyIds) AND expr.size IS NOT NULL " +
			"GROUP BY expr.dataset.datasetAcquisition.examination.study.id, expr.datasetExpressionFormat")
	List<Object[]> findExpressionSizesTotalByStudyIdGroupByFormat(List<Long> studyIds);

    List<Dataset> deleteByDatasetProcessingId(Long id);

	boolean existsByTagsContains(StudyTag tag);

	@Query(value="SELECT ds.id FROM dataset as ds " +
			"INNER JOIN input_of_dataset_processing as input ON ds.id=input.dataset_id " +
			"WHERE input.processing_id = :processingId or ds.dataset_processing_id = :processingId", nativeQuery = true)
	List<Dataset> findDatasetsByProcessingId(Long processingId);
}