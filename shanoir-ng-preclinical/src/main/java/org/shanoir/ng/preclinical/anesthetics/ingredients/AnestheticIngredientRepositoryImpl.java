package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;


@Component
public class AnestheticIngredientRepositoryImpl implements AnestheticIngredientRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
		
	@SuppressWarnings("unchecked")
	@Override
	public List<AnestheticIngredient> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT i FROM AnestheticIngredient i WHERE i." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	} 
	

}
