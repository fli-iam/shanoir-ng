package org.shanoir.ng.dataset;

import org.shanoir.ng.shared.exception.ShanoirDatasetException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Dataset service.
 *
 * @author msimon
 *
 */
public interface DatasetService extends UniqueCheckableService<Dataset> {

	/**
	 * Delete a dataset.
	 * 
	 * @param id
	 *            dataset id.
	 * @throws ShanoirDatasetException
	 */
	void deleteById(Long id) throws ShanoirDatasetException;

	/**
	 * Find dataset by its id.
	 *
	 * @param id
	 *            dataset id.
	 * @return a dataset or null.
	 */
	Dataset findById(Long id);

	/**
	 * Save a dataset.
	 *
	 * @param dataset
	 *            dataset to create.
	 * @return created dataset.
	 * @throws ShanoirDatasetException
	 */
	Dataset save(Dataset dataset) throws ShanoirDatasetException;

	/**
	 * Update a dataset.
	 *
	 * @param dataset
	 *            dataset to update.
	 * @return updated dataset.
	 * @throws ShanoirDatasetException
	 */
	Dataset update(Dataset dataset) throws ShanoirDatasetException;

	/**
	 * Update a dataset from the old Shanoir
	 * 
	 * @param dataset
	 *            dataset.
	 * @throws ShanoirDatasetException
	 */
	void updateFromShanoirOld(Dataset dataset) throws ShanoirDatasetException;

}
