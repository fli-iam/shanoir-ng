package org.shanoir.ng.preclinical.therapies;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;


@Component
public class TherapyRepositoryImpl implements TherapyRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
		
	@SuppressWarnings("unchecked")
	@Override
	public List<Therapy> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT t FROM Therapy t WHERE t." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}
	
}
