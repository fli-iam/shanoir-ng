package org.shanoir.ng.dataset;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for datasets.
 * 
 * @author msimon
 *
 */
@Component
public class DatasetRepositoryImpl implements DatasetRepositoryCustom {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT d FROM Dataset d WHERE d." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}

}
