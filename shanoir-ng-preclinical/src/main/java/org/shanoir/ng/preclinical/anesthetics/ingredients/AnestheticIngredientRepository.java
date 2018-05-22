package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.springframework.data.repository.CrudRepository;


public interface AnestheticIngredientRepository extends CrudRepository<AnestheticIngredient, Long>, AnestheticIngredientRepositoryCustom{

	List<AnestheticIngredient> findByAnesthetic(Anesthetic anesthetic);
}
