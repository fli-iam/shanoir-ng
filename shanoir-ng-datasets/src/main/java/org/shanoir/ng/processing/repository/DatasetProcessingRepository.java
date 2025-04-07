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
import org.springframework.stereotype.Repository;

/**
 * Repository for dataset processings.
 *
 * @author msimon
 */
@Repository
public interface DatasetProcessingRepository extends CrudRepository<DatasetProcessing, Long> {

	/**
	 * Find dataset processing by name.
	 *
	 * @param comment comment.
	 * @return a dataset processing.
	 */
	Optional<DatasetProcessing> findByComment(String comment);


	/**
	 * Find all dataset processing by comment and type.
	 *
	 * @param comment Comment.
	 * @param type Dataset processing type.
	 * @return List of dataset processing.
	 */
	@Query(value="SELECT DISTINCT processing.id FROM dataset_processing as processing " +
			"INNER JOIN execution_monitoring AS monitoring ON monitoring.id = processing.id " +
			"WHERE processing.dataset_processing_type = :type " +
			"AND processing.comment LIKE :comment " +
			"AND monitoring.name LIKE '%post_processing' " +
			"AND monitoring.status = 1", nativeQuery = true)
	List<Long> findIdsByCommentAndDatasetProcessingTypeWithStatusFinished(String comment, int type);

	/**
	 * Find all processings that are linked to given dataset through INPUT_OF_DATASET_PROCESSING table
	 *
	 * @param datasetId
	 * @return
	 */
	List<DatasetProcessing> findAllByInputDatasets_Id(Long datasetId);

	/**
	 * Find all processings that are linked to given parent processing id
	 *
	 * @param id
	 * @return
	 */
    List<DatasetProcessing> findAllByParentId(Long id);
	
	/**
	 * Find all processings that are linked to given examinations
	 *
	 * @param examinationIds
	 * @return
	 */
	@Query(value="SELECT DISTINCT processing.id FROM dataset_processing as processing " +
			"INNER JOIN input_of_dataset_processing as input ON processing.id=input.processing_id " +
			"INNER JOIN dataset as dataset ON dataset.id=input.dataset_id " +
			"INNER JOIN dataset_acquisition as acquisition ON acquisition.id=dataset.dataset_acquisition_id " +
			"WHERE acquisition.examination_id IN (:examinationIds)", nativeQuery = true)
	List<Long> findAllIdsByExaminationIds(List<Long> examinationIds);
}
