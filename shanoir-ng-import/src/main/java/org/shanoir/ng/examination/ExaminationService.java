package org.shanoir.ng.examination;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirExaminationException;
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
	 * @throws ShanoirExaminationException
	 */
	void deleteById(Long id) throws ShanoirExaminationException;

	/**
	 * Get all the examinations.
	 * 
	 * @return a list of examinations.
	 */
	List<Examination> findAll();

	/**
	 * Find examination by data.
	 *
	 * @param data
	 *            data.
	 * @return a examination.
	 */
//	Optional<Examination> findByData(String data);

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
	 * @throws ShanoirExaminationException
	 */
	Examination save(Examination examination) throws ShanoirExaminationException;

	/**
	 * Update an examination.
	 *
	 * @param examination
	 *            examination to update.
	 * @return updated examination.
	 * @throws ShanoirExaminationException
	 */
	Examination update(Examination examination) throws ShanoirExaminationException;

	/**
	 * Update an examination from the old Shanoir
	 * 
	 * @param examination
	 *            examination.
	 * @throws ShanoirExaminationException
	 */
	void updateFromShanoirOld(Examination examination) throws ShanoirExaminationException;

}
