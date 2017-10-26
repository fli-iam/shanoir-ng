package org.shanoir.ng.examination;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for Examination.
 * 
 * @author ifakhfakh
 *
 */
@Component
public class ExaminationRepositoryImpl implements ExaminationRepositoryCustom {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Examination> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT t FROM Examination t WHERE t." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}

}
