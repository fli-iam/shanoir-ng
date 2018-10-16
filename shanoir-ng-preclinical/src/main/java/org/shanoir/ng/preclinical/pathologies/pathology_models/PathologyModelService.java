package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.shared.exception.ShanoirException;
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
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

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
	 * @throws ShanoirException
	 */
	PathologyModel save(PathologyModel model) throws ShanoirException;

	/**
	 * Update a reference.
	 *
	 * @param reference
	 *            reference to update.
	 * @return updated reference.
	 * @throws ShanoirException
	 */
	PathologyModel update(PathologyModel model) throws ShanoirException;

	

}
