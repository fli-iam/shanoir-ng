package org.shanoir.ng.studycard;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.model.ItemRepositoryCustom;
import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for templates.
 * 
 * @author msimon
 *
 */
@Component
public class StudyCardRepositoryImpl implements ItemRepositoryCustom<StudyCard> {

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
