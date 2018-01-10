package org.shanoir.ng.subject;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.model.ItemRepositoryCustom;
import org.springframework.stereotype.Repository;

/**
 * Implementation of custom repository for templates.
 * 
 * @author msimon
 *
 */
@Repository
public class SubjectRepositoryImpl implements ItemRepositoryCustom<Subject> {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Subject> findBy(String fieldName, Object value) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT s FROM Subject s WHERE s.").append(fieldName).append(" LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}

}
