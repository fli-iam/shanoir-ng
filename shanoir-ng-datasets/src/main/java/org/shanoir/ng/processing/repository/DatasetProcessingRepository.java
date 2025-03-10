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

package org.shanoir.ng.processing.repository;

import java.util.List;
import java.util.Optional;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for dataset processings.
 *
 * @author msimon
 */
public interface DatasetProcessingRepository extends CrudRepository<DatasetProcessing, Long> {

	/**
	 * Find dataset processing by name.
	 *
	 * @param comment comment.
	 * @return a dataset processing (Optional).
	 */
	Optional<DatasetProcessing> findByComment(String comment);

	/**
	 * Find all processings that are linked to given dataset through INPUT_OF_DATASET_PROCESSING table
	 *
	 * @param datasetId
	 * @return list of processing
	 */
	List<DatasetProcessing> findAllByInputDatasets_Id(Long datasetId);

	/**
	 * Find all processings ids that are linked to given datasets through INPUT_OF_DATASET_PROCESSING table
	 *
	 * @param datasetIds
	 * @return list of processing ids
	 */
	@Query(value="SELECT DISTINCT processing.id FROM dataset_processing as processing " +
			"INNER JOIN input_of_dataset_processing as input ON processing.id=input.processing_id " +
			"WHERE input.dataset_id IN (:datasetIds)", nativeQuery = true)
	List<Long> findAllIdsByInputDatasets_Ids(List<Long> datasetIds);

	/**
	 * Find all processings ids that are linked to given processing id
	 *
	 * @param id
	 * @return list of processing
	 */
	List<DatasetProcessing> findAllByParentId(Long id);
	
	/**
	 * Find all processings that are linked to given examinations
	 *
	 * @param examinationIds
	 * @return list of processing ids
	 */
	@Query(value="SELECT DISTINCT processing.id FROM dataset_processing as processing " +
			"INNER JOIN input_of_dataset_processing as input ON processing.id=input.processing_id " +
			"INNER JOIN dataset as dataset ON dataset.id=input.dataset_id " +
			"INNER JOIN dataset_acquisition as acquisition ON acquisition.id=dataset.dataset_acquisition_id " +
			"WHERE acquisition.examination_id IN (:examinationIds)", nativeQuery = true)
	List<Long> findAllIdsByExaminationIds(List<Long> examinationIds);

	/**
	 * Find all processings that are linked to given studies
	 *
	 * @param studyIds
	 * @return list of processing ids
	 */
	@Query(value="SELECT DISTINCT processing.id FROM dataset_processing as processing " +
			"INNER JOIN input_of_dataset_processing as input ON processing.id=input.processing_id " +
			"INNER JOIN dataset as dataset ON dataset.id=input.dataset_id " +
			"INNER JOIN dataset_acquisition as acquisition ON acquisition.id=dataset.dataset_acquisition_id " +
			"INNER JOIN examination as examination ON examination.id = acquisition.examination_id " +
			"WHERE examination.study_id IN (:studyIds)", nativeQuery = true)
	List<Long>findAllIdsByStudyIds(List<Long> studyIds);

	/**
	 * Find all processings that are linked to given acquisitions
	 *
	 * @param acquisitionIds
	 * @return list of processing ids
	 */
	@Query(value="SELECT DISTINCT processing.id FROM dataset_processing as processing " +
			"INNER JOIN input_of_dataset_processing as input ON processing.id=input.processing_id " +
			"INNER JOIN dataset as dataset ON dataset.id=input.dataset_id " +
			"WHERE dataset.dataset_acquisition_id IN (:acquisitionIds)", nativeQuery = true)
	List<Long>findAllIdsByAcquisitionIds(List<Long> acquisitionIds);

	/**
	 * Find all processings that are linked to given subjects
	 *
	 * @param subjectIds
	 * @return list of processing ids
	 */
	@Query(value="SELECT DISTINCT processing.id FROM dataset_processing as processing " +
			"INNER JOIN input_of_dataset_processing as input ON processing.id=input.processing_id " +
			"INNER JOIN dataset as dataset ON dataset.id=input.dataset_id " +
			"INNER JOIN dataset_acquisition as acquisition ON acquisition.id=dataset.dataset_acquisition_id " +
			"INNER JOIN examination as examination ON examination.id = acquisition.examination_id " +
			"WHERE examination.subject_id IN (:subjectIds)", nativeQuery = true)
	List<Long>findAllIdsBySubjectIds(List<Long> subjectIds);

	/**
	 * Filter all processing according to a specific pipelineIdentifier
	 *
	 * @param processingIds list of processing to filter
	 * @param pipelineIdentifier pipelineIdentifier to filter
	 * @return list of processing ids
	 */
	@Query(value="SELECT DISTINCT processing.id FROM dataset_processing as processing " +
			"WHERE processing.comment LIKE :pipelineIdentifier " +
			"AND processing.id IN (:processingIds)", nativeQuery = true)
	List<Long>filterIdsByIdentifier(List<Long> processingIds, String pipelineIdentifier);
}
