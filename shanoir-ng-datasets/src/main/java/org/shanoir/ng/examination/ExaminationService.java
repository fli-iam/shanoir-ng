package org.shanoir.ng.examination;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;
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
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the examinations.
	 * 
	 * @return a list of examinations.
	 */
	List<Examination> findAll() throws ShanoirException;

	/**
	 * Find examination by its id.
	 *
	 * @param id
	 *            examination id.
	 * @return an examination or null.
	 * @throws ShanoirException 
	 */
	Examination findById(Long id) throws ShanoirException;

	/**
	 * Save an examination.
	 *
	 * @param examination
	 *            examination to create.
	 * @return created examination.
	 * @throws ShanoirException
	 */
	Examination save(Examination examination) throws ShanoirException;

	/**
	 * Update an examination.
	 *
	 * @param examination
	 *            examination to update.
	 * @return updated examination.
	 * @throws ShanoirException
	 */
	Examination update(Examination examination) throws ShanoirException;

	/**
	 * Update an examination from the old Shanoir
	 * 
	 * @param examination
	 *            examination.
	 * @throws ShanoirException
	 */
	void updateFromShanoirOld(Examination examination) throws ShanoirException;

	/**
	 * @param subjectId
	 * @return
	 * @author yyao
	 */
	List<Examination> findBySubjectId(Long subjectId);

}
