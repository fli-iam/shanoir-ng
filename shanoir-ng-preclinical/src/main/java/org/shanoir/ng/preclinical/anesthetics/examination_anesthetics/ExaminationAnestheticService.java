package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
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
	 * @throws ShanoirPreclinicalException
	 */
	void deleteById(Long id) throws ShanoirPreclinicalException;

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
	 * @param examination anesthetic
	 *            examination anesthetic to create.
	 * @return created ExaminationAnesthetic.
	 * @throws ShanoirPreclinicalException
	 */
	ExaminationAnesthetic save(ExaminationAnesthetic examAnesthetic) throws ShanoirPreclinicalException;

	/**
	 * Update a examination anesthetic
	 *
	 * @param examination anesthetic
	 *            examination anesthetic to update.
	 * @return updated ExaminationAnesthetic.
	 * @throws ShanoirPreclinicalException
	 */
	ExaminationAnesthetic update(ExaminationAnesthetic examAnesthetic) throws ShanoirPreclinicalException;

	

}
