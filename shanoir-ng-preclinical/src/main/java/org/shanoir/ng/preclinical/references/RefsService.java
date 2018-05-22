package org.shanoir.ng.preclinical.references;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

import org.shanoir.ng.preclinical.references.Reference;

/**
 * Refs service.
 *
 * @author sloury
 *
 */
public interface RefsService extends UniqueCheckableService<Reference> {

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
	List<Reference> findAll();

	/**
	 * Find references by category.
	 *
	 * @param category
	 *            category.
	 * @return a list of references.
	 */
	List<Reference> findByCategory(String category);
	
	/**
	 * Find references by category and type.
	 * @param category
	 *            category.
	 * @param type
	 *            type.
	 * @return a list of references.
	 */
	List<Reference> findByCategoryAndType(String category, String type);
	
	/**
	 * Find existing references categories
	 * 
	 * @return a list of string types.
	 */
	List<String> findCategories();
	
	/**
	 * Find existing references types by given category
	 * 
	 * @param category
	 *            category.
	 * @return a list of string types.
	 */
	List<String> findTypesByCategory(String category);
	
	/**
	 * Find reference by category, type and value
	 *
	 * @param category
	 * @param type
	 * @param value
	 * @return a reference.
	 */
	Reference findByCategoryTypeAndValue(String category, String type, String value);
	
	/**
	 * Find reference by type and value
	 * Used in validation
	 *
	 * @param type
	 * @param value
	 * @return a reference.
	 */
	Optional<Reference> findByTypeAndValue(String type, String value);

	/**
	 * Find reference by its id.
	 *
	 * @param id
	 *            reference id.
	 * @return a reference or null.
	 */
	Reference findById(Long id);

	/**
	 * Save a reference.
	 *
	 * @param reference
	 *            reference to create.
	 * @return created reference.
	 * @throws ShanoirPreclinicalException
	 */
	Reference save(Reference reference) throws ShanoirPreclinicalException;

	/**
	 * Update a reference.
	 *
	 * @param reference
	 *            reference to update.
	 * @return updated reference.
	 * @throws ShanoirPreclinicalException
	 */
	Reference update(Reference reference) throws ShanoirPreclinicalException;

	

}
