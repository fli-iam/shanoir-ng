package org.shanoir.ng.repository.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.model.Center;
import org.shanoir.ng.repository.CenterRepositoryCustom;
import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for centers.
 * 
 * @author msimon
 *
 */
@Component
public class CenterRepositoryImpl implements CenterRepositoryCustom {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Center> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT t FROM center t WHERE t." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}

}
