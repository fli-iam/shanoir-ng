package org.shanoir.ng.shared.validation;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.springframework.stereotype.Component;

/**
 * Custom repository for entities.
 * 
 * @author msimon
 *
 */
@Component
public class FindByRepositoryImpl<T extends AbstractEntity> implements FindByRepository<T> {

	@PersistenceContext
	private EntityManager em;


	@SuppressWarnings("unchecked")
	@Override
	public List<T> findBy(String fieldName, Object value, @SuppressWarnings("rawtypes") Class clazz) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT e FROM " + clazz.getSimpleName() + " e WHERE e.").append(fieldName).append(" LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}

}
