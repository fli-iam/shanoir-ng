package org.shanoir.ng.template;

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
public class TemplateRepositoryImpl implements ItemRepositoryCustom<Template> {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Template> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT t FROM Template t WHERE t." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}

}
