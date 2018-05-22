package org.shanoir.ng.preclinical.contrast_agent;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;


@Component
public class ContrastAgentRepositoryImpl implements ContrastAgentRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
		
	@SuppressWarnings("unchecked")
	@Override
	public List<ContrastAgent> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT ca FROM ContrastAgent ca WHERE ca." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}
		
	
}
