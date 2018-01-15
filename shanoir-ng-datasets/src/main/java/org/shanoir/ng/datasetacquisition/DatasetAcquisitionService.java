package org.shanoir.ng.datasetacquisition;

import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.exception.ShanoirException;

/**
 * dataset acquisition service.
 * 
 * @author atouboul
 *
 */

public interface DatasetAcquisitionService<T extends DatasetAcquisition> {
	
	/**
	 * Delete a DatasetAcquisition.
	 * 
	 * @param id
	 *            DatasetAcquisition id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Find DatasetAcquisition by its id.
	 *
	 * @param id
	 *            DatasetAcquisition id.
	 * @return a DatasetAcquisition or null.
	 */
	T findById(Long id);

	/**
	 * Save a DatasetAcquisition.
	 *
	 * @param DatasetAcquisition
	 *            DatasetAcquisition to create.
	 * @return created DatasetAcquisition.
	 * @throws ShanoirException
	 */
	T save(T datasetAcquisition) throws ShanoirException;

	/**
	 * Update a DatasetAcquisition.
	 *
	 * @param DatasetAcquisition
	 *            DatasetAcquisition to update.
	 * @return updated DatasetAcquisition.
	 * @throws ShanoirException
	 */
	T update(T dataset) throws ShanoirException;

	
	void createDatasetAcquisition(Serie serie, int rank, Examination examination);
	
}
