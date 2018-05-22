package org.shanoir.ng.preclinical.pathologies;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;


@Component
public class PathologyRepositoryImpl implements PathologyRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
		
	@SuppressWarnings("unchecked")
	@Override
	public List<Pathology> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT p FROM Pathology p WHERE p." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}
	
	
}
