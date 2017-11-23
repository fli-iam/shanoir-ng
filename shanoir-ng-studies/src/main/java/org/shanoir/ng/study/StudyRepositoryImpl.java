package org.shanoir.ng.study;

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
public class StudyRepositoryImpl implements StudyRepositoryCustom {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Study> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT s FROM Study s WHERE s." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<StudyNameDTO> findIdsAndNames() {
		return em.createNativeQuery(
				"SELECT id, name FROM study", "studyNameResult")
				.getResultList();
	}

}
