package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

/**
 * Custom repository for anesthetic ingredients.
 * 
 * @author sloury
 *
 */
public interface AnestheticIngredientRepositoryCustom {
	
	List<AnestheticIngredient> findBy(String fieldName, Object value);
	
	

}
