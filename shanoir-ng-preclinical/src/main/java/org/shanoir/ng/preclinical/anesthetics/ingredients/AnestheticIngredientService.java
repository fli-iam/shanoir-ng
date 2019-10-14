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

package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;



/**
 * Anesthetic ingredient service.
 *
 * @author sloury
 *
 */
public interface AnestheticIngredientService extends UniqueCheckableService<AnestheticIngredient> {

	/**
	 * Delete an ingredient.
	 * 
	 * @param id
	 *            ingredient id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the ingredients.
	 * 
	 * @return a list of ingredients.
	 */
	List<AnestheticIngredient> findAll();

	/**
	 * Get all the ingredients according to given anesthetic.
	 * 
	 * @param anesthetic
	 *            Anesthteic.
	 * @return a list of ingredients.
	 */
	List<AnestheticIngredient> findByAnesthetic(Anesthetic anesthetic);
	
	/**
	 * Find Ingredient by its id.
	 *
	 * @param id
	 *            Ingredient id.
	 * @return a Ingredient or null.
	 */
	AnestheticIngredient findById(Long id);
	
	
	/**
	 * Save an ingredient.
	 *
	 * @param ingredient
	 *            ingredient to create.
	 * @return created ingredient.
	 * @throws ShanoirException
	 */
	AnestheticIngredient save(AnestheticIngredient ingredient) throws ShanoirException;

	/**
	 * Update a ingredient.
	 *
	 * @param ingredient
	 *            ingredient to update.
	 * @return updated ingredient.
	 * @throws ShanoirException
	 */
	AnestheticIngredient update(AnestheticIngredient ingredient) throws ShanoirException;

	

}
