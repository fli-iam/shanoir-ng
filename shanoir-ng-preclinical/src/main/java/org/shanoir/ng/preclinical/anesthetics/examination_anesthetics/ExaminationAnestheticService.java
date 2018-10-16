package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Examination anesthetic service.
 *
 * @author sloury
 *
 */
public interface ExaminationAnestheticService extends UniqueCheckableService<ExaminationAnesthetic> {

	/**
	 * Delete an examination anesthetic
	 * 
	 * @param id
	 *            examination anesthetic id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the examination anesthetics
	 * 
	 * @return a list of examination anesthetics
	 */
	List<ExaminationAnesthetic> findAll();

	/**
	 * Get all the examination anesthetics by examination id
	 * 
	 * @return a list of examination anesthetics
	 */
	List<ExaminationAnesthetic> findByExaminationId(Long examinationId);

	/**
	 * Find examination anesthetic by its id.
	 *
	 * @param id
	 *            examination anesthetic id.
	 * @return a examination anesthetic or null.
	 */
	ExaminationAnesthetic findById(Long id);

	/**
	 * Save an examination anesthetic
	 *
	 * @param examination
	 *            anesthetic examination anesthetic to create.
	 * @return created ExaminationAnesthetic.
	 * @throws ShanoirException
	 */
	ExaminationAnesthetic save(ExaminationAnesthetic examAnesthetic) throws ShanoirException;

	/**
	 * Update a examination anesthetic
	 *
	 * @param examination
	 *            anesthetic examination anesthetic to update.
	 * @return updated ExaminationAnesthetic.
	 * @throws ShanoirException
	 */
	ExaminationAnesthetic update(ExaminationAnesthetic examAnesthetic) throws ShanoirException;

	/**
	 * Get all the examination anesthetics by anesthetic
	 * 
	 * @return a list of examination anesthetics
	 */
	List<ExaminationAnesthetic> findByAnesthetic(Anesthetic anesthetic);

}
