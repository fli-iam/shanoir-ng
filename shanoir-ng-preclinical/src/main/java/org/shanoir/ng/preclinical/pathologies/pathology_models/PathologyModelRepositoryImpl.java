package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.springframework.stereotype.Component;


@Component
public class PathologyModelRepositoryImpl implements PathologyModelRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
		
	@SuppressWarnings("unchecked")
	@Override
	public List<PathologyModel> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT p FROM PathologyModel p WHERE p." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PathologyModel> findByPathology(Pathology pathology) {
		return em.createQuery(
				"SELECT pm FROM PathologyModel pm LEFT JOIN pm.pathology p WHERE p.id = :id")
				.setParameter("id", pathology.getId())
				.getResultList();
	}
}
