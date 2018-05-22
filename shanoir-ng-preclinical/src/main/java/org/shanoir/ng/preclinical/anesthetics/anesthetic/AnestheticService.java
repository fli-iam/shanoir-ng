package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;



/**
 * Anesthetics service.
 *
 * @author sloury
 *
 */
public interface AnestheticService extends UniqueCheckableService<Anesthetic> {

	/**
	 * Delete an anesthetic.
	 * 
	 * @param id
	 *            anesthetic id.
	 * @throws ShanoirPreclinicalException
	 */
	void deleteById(Long id) throws ShanoirPreclinicalException;

	/**
	 * Get all the anesthetics.
	 * 
	 * @return a list of anesthetics.
	 */
	List<Anesthetic> findAll();
	
	/**
	 * Get all the anesthetics by type.
	 * 
	 * @return a list of anesthetics.
	 */
	List<Anesthetic> findAllByAnestheticType(AnestheticType type);

	
	/**
	 * Find anesthetic by its id.
	 *
	 * @param id
	 *            anesthetic id.
	 * @return a anesthetic or null.
	 */
	Anesthetic findById(Long id);
	
	
	/**
	 * Save an anesthetic.
	 *
	 * @param anesthetic
	 *            anesthetic to create.
	 * @return created anesthetic.
	 * @throws ShanoirPreclinicalException
	 */
	Anesthetic save(Anesthetic anesthetic) throws ShanoirPreclinicalException;

	/**
	 * Update a anesthetic.
	 *
	 * @param anesthetic
	 *            anesthetic to update.
	 * @return updated anesthetic.
	 * @throws ShanoirPreclinicalException
	 */
	Anesthetic update(Anesthetic anesthetic) throws ShanoirPreclinicalException;

	

}
