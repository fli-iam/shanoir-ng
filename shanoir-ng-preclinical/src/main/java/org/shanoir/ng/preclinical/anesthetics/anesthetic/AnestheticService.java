package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;
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
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

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
	 * @throws ShanoirException
	 */
	Anesthetic save(Anesthetic anesthetic) throws ShanoirException;

	/**
	 * Update a anesthetic.
	 *
	 * @param anesthetic
	 *            anesthetic to update.
	 * @return updated anesthetic.
	 * @throws ShanoirException
	 */
	Anesthetic update(Anesthetic anesthetic) throws ShanoirException;

	

}
