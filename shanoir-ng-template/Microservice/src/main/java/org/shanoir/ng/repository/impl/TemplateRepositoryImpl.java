package org.shanoir.ng.repository.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.model.Template;
import org.shanoir.ng.repository.TemplateRepositoryCustom;
import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for templates.
 * 
 * @author msimon
 *
 */
@Component
public class TemplateRepositoryImpl implements TemplateRepositoryCustom {

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
