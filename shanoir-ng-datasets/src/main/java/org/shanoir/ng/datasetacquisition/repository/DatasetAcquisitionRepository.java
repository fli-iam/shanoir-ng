package org.shanoir.ng.datasetacquisition.repository;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for dataset acquisition.
 * 
 * @author msimon
 *
 */
public interface DatasetAcquisitionRepository extends CrudRepository<DatasetAcquisition, Long> {

}
