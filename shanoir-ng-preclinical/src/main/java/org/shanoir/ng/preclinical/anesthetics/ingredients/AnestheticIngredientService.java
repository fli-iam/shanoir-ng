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
