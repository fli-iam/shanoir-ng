package org.shanoir.ng.preclinical.pathologies;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;



/**
 * Pathologies service.
 *
 * @author sloury
 *
 */
public interface PathologyService extends UniqueCheckableService<Pathology> {

	/**
	 * Delete a reference value.
	 * 
	 * @param id
	 *            template id.
	 * @throws ShanoirPreclinicalException
	 */
	void deleteById(Long id) throws ShanoirPreclinicalException;

	/**
	 * Get all the references.
	 * 
	 * @return a list of references.
	 */
	List<Pathology> findAll();

	
	/**
	 * Find reference by its id.
	 *
	 * @param id
	 *            reference id.
	 * @return a reference or null.
	 */
	Pathology findById(Long id);
	
	Optional<Pathology> findByName(String name);

	/**
	 * Save a reference.
	 *
	 * @param reference
	 *            reference to create.
	 * @return created reference.
	 * @throws ShanoirPreclinicalException
	 */
	Pathology save(Pathology pathology) throws ShanoirPreclinicalException;

	/**
	 * Update a reference.
	 *
	 * @param reference
	 *            reference to update.
	 * @return updated reference.
	 * @throws ShanoirPreclinicalException
	 */
	Pathology update(Pathology pathology) throws ShanoirPreclinicalException;

	/*
	 * Update Shanoir Old with new pathology.
	 *
	 * @param Pathology pathology.
	 *
	 * @return false if it fails, true if it succeed.
	 */
	boolean updateFromShanoirOld(final Pathology pathology);
	

}
