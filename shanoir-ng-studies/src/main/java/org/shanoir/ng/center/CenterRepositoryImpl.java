package org.shanoir.ng.center;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
				"SELECT c FROM Center c WHERE c." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CenterNameDTO> findIdsAndNames() {
		return em.createNativeQuery(
				"SELECT id, name FROM center", "centerNameResult")
				.getResultList();
	}

}
