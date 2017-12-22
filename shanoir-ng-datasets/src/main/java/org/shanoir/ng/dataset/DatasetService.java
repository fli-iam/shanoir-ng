package org.shanoir.ng.dataset;

import org.shanoir.ng.shared.exception.ShanoirException;
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
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

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
	 * @throws ShanoirException
	 */
	Dataset save(Dataset dataset) throws ShanoirException;

	/**
	 * Update a dataset.
	 *
	 * @param dataset
	 *            dataset to update.
	 * @return updated dataset.
	 * @throws ShanoirException
	 */
	Dataset update(Dataset dataset) throws ShanoirException;

	/**
	 * Update a dataset from the old Shanoir
	 * 
	 * @param dataset
	 *            dataset.
	 * @throws ShanoirException
	 */
	void updateFromShanoirOld(Dataset dataset) throws ShanoirException;

}
