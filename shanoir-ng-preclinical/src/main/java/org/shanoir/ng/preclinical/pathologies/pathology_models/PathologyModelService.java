package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;



/**
 * Pathology models service.
 *
 * @author sloury
 *
 */
public interface PathologyModelService extends UniqueCheckableService<PathologyModel> {

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
	List<PathologyModel> findAll();

	
	/**
	 * Find reference by its id.
	 *
	 * @param id
	 *            reference id.
	 * @return a reference or null.
	 */
	PathologyModel findById(Long id);
	
	
	List<PathologyModel> findByPathology(Pathology pathology);

	/**
	 * Save a reference.
	 *
	 * @param reference
	 *            reference to create.
	 * @return created reference.
	 * @throws ShanoirPreclinicalException
	 */
	PathologyModel save(PathologyModel model) throws ShanoirPreclinicalException;

	/**
	 * Update a reference.
	 *
	 * @param reference
	 *            reference to update.
	 * @return updated reference.
	 * @throws ShanoirPreclinicalException
	 */
	PathologyModel update(PathologyModel model) throws ShanoirPreclinicalException;

	

}
