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
import java.util.function.LongToIntFunction;

import org.shanoir.ng.processing.model.DatasetProcessing;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for dataset processings.
 *
 * @author msimon
 */
public interface DatasetProcessingRepository extends CrudRepository<DatasetProcessing, Long> {

    /**
     * Find dataset processings by ids.
     *
     * @param ids list of id.
     * @return a list of dataset processing.
     */
    @Query("SELECT processing FROM DatasetProcessing processing " +
            "WHERE processing.id IN :ids ")
    List<DatasetProcessing> findByIds(List<Long> ids);

    /**
     * Find dataset processings by monitoring id.
     *
     * @param id monitoring id.
     * @return a list of dataset processing.
     */
    @Query("SELECT processing FROM DatasetProcessing processing " +
            "WHERE processing.parent.id = :id ")
    List<DatasetProcessing> findByMonitoringId(Long id);

    /**
     * Find all processings that are linked to given dataset through INPUT_OF_DATASET_PROCESSING table
     *
     * @param datasetId
     * @return
     */
    List<DatasetProcessing> findAllByInputDatasets_Id(Long datasetId);

    /**
     * Find all processings that are linked to the given datasets through INPUT_OF_DATASET_PROCESSING
     * table, in one single query, with the output datasets fetched along
     *
     * @param datasetIds
     * @return
     */
    @EntityGraph(attributePaths = "outputDatasets")
    List<DatasetProcessing> findAllByInputDatasets_IdIn(List<Long> datasetIds);

    /**
     * Find all dataset processing by comment and type.
     *
     * @param comment Comment.
     * @param type Dataset processing type.
     * @return List of dataset processing.
     */
    @Query(value = "WITH candidates AS ( "
            + "SELECT processing.id AS id "
            + "FROM dataset_processing AS processing "
            + "    INNER JOIN execution_monitoring AS monitoring ON monitoring.id = processing.id "
            + "WHERE processing.dataset_processing_type = :type "
            + "    AND processing.comment LIKE :comment "
            + "    AND monitoring.name LIKE '%post_processing' "
            + "    AND monitoring.status = 1), "
            + "processing_inputs AS ( "
            + "SELECT proc.id AS processing_id, "
            + "    proc.parent_id AS monitoring_id, "
            + "    GROUP_CONCAT(inputs.dataset_id ORDER BY inputs.dataset_id) AS input_signature "
            + "FROM input_of_dataset_processing AS inputs "
            + "    INNER JOIN dataset_processing proc ON proc.id = inputs.processing_id"
            + "    INNER JOIN candidates c ON c.id = proc.parent_id "
            + "GROUP BY proc.id ), "
            + "ranked AS ( "
            + "SELECT pi.processing_id AS id, "
            + "    ROW_NUMBER() OVER (PARTITION BY pi.input_signature ORDER BY monitoring.start_date DESC) AS rn "
            + "FROM processing_inputs pi "
            + "    INNER JOIN execution_monitoring monitoring ON monitoring.id = pi.monitoring_id) "
            + "SELECT id FROM ranked WHERE rn = 1 ORDER BY id", nativeQuery = true)
    List<Long> findIdsByCommentAndDatasetProcessingTypeWithStatusFinished(String comment, int type);

    /**
     * Find all processings that are linked to given monitoring through parent_id column
     *
     * @param monitoringId
     * @return
     */
    @Query(value = "SELECT DISTINCT processing.id FROM dataset_processing as processing "
            + "WHERE processing.parent_id = :monitoringId", nativeQuery = true)
    List<Long> findAllIdsByMonitoringId(Long monitoringId);

    List<DatasetProcessing> findAllByParentId(Long id);

    /**
     * Find all processings that are linked to given examinations
     *
     * @param examinationIds
     * @return
     */
    @Query(value = "SELECT DISTINCT processing.id FROM dataset_processing as processing "
            + "INNER JOIN input_of_dataset_processing as input ON processing.id=input.processing_id "
            + "INNER JOIN dataset as dataset ON dataset.id=input.dataset_id "
            + "INNER JOIN dataset_acquisition as acquisition ON acquisition.id=dataset.dataset_acquisition_id "
            + "WHERE acquisition.examination_id IN (:examinationIds)", nativeQuery = true)
    List<Long> findAllIdsByExaminationIds(List<Long> examinationIds);

    @Query("SELECT processing FROM DatasetProcessing processing " +
            "JOIN FETCH processing.inputDatasets " +
            "WHERE processing.id = :id")
    Optional<DatasetProcessing> findByIdWithInputs(Long Id);

    @Query("SELECT processing FROM DatasetProcessing processing " +
            "JOIN FETCH processing.outputDatasets " +
            "WHERE processing.id = :id")
    Optional<DatasetProcessing> findByIdWithOutputs(Long Id);

    @Query("SELECT processing FROM DatasetProcessing processing " +
            "JOIN FETCH processing.outputDatasets " +
            "JOIN FETCH processing.inputDatasets " +
            "WHERE processing.id = :id")
    Optional<DatasetProcessing> findByIdWithInputsAndOutputs(Long Id);

    @Query("SELECT processing FROM DatasetProcessing processing " +
            "JOIN FETCH processing.outputDatasets " +
            "JOIN FETCH processing.inputDatasets " +
            "WHERE processing.id IN :ids")
    List<DatasetProcessing> findByIdsWithInputsAndOutputs(List<Long> Ids);

    @Query("SELECT DISTINCT p FROM DatasetProcessing p " +
            "JOIN FETCH p.inputDatasets " +
            "WHERE EXISTS (SELECT i FROM p.inputDatasets i WHERE i.id = :inputId)")
    List<DatasetProcessing> findByInputIdWithInputs(Long inputId);

    /**
     * Find all identifying fields for a given processing id
     *
     * @param processingId
     * @return
     */
    @Query(value = "SELECT processing.monitoring_index as monitoringIndex, monitoring.identifier as monitoringIdentifier FROM dataset_processing as processing "
            + "INNER JOIN dataset_processing as parent on parent.id = processing.parent_id "
            + "INNER JOIN execution_monitoring as monitoring on monitoring.id = parent.id "
            + "WHERE processing.id = :processingId", nativeQuery = true)
    IdentificationData findIdentificationDataFromProcessingId(Long processingId);

    interface IdentificationData {
        Long getMonitoringIndex();
        String getMonitoringIdentifier();
    }
}
