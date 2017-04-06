package org.shanoir.ng.studyCards;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for templates.
 * 
 * @author msimon
 *
 */
@Component
public class StudyCardRepositoryImpl implements StudyCardRepositoryCustom {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	//@Override
	public List<StudyCard> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT sc FROM StudyCard sc WHERE sc." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}

}
