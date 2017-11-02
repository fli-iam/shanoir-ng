package org.shanoir.ng.coil;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for coils.
 * 
 * @author msimon
 *
 */
@Component
public class CoilRepositoryImpl implements CoilRepositoryCustom {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Coil> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT c FROM Coil c WHERE c." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}

}
