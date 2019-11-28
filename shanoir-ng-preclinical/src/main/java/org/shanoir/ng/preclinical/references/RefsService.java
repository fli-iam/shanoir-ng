/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.preclinical.references;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirException;
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
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

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
	 * @throws ShanoirException
	 */
	Reference save(Reference reference) throws ShanoirException;

	/**
	 * Update a reference.
	 *
	 * @param reference
	 *            reference to update.
	 * @return updated reference.
	 * @throws ShanoirException
	 */
	Reference update(Reference reference) throws ShanoirException;

	

}
