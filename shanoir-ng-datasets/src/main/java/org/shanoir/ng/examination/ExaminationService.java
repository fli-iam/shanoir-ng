package org.shanoir.ng.examination;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirDatasetException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Examination service.
 *
 * @author ifakhfakh
 *
 */
public interface ExaminationService extends UniqueCheckableService<Examination> {

	/**
	 * Delete an examination.
	 * 
	 * @param id
	 *            examination id.
	 * @throws ShanoirDatasetException
	 */
	void deleteById(Long id) throws ShanoirDatasetException;

	/**
	 * Get all the examinations.
	 * 
	 * @return a list of examinations.
	 */
	List<Examination> findAll();

	/**
	 * Find examination by its id.
	 *
	 * @param id
	 *            examination id.
	 * @return an examination or null.
	 */
	Examination findById(Long id);

	/**
	 * Save an examination.
	 *
	 * @param examination
	 *            examination to create.
	 * @return created examination.
	 * @throws ShanoirDatasetException
	 */
	Examination save(Examination examination) throws ShanoirDatasetException;

	/**
	 * Update an examination.
	 *
	 * @param examination
	 *            examination to update.
	 * @return updated examination.
	 * @throws ShanoirDatasetException
	 */
	Examination update(Examination examination) throws ShanoirDatasetException;

	/**
	 * Update an examination from the old Shanoir
	 * 
	 * @param examination
	 *            examination.
	 * @throws ShanoirDatasetException
	 */
	void updateFromShanoirOld(Examination examination) throws ShanoirDatasetException;

}
