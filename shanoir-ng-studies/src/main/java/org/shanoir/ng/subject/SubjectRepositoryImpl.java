package org.shanoir.ng.subject;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for templates.
 * 
 * @author msimon
 *
 */
@Component
public class SubjectRepositoryImpl implements SubjectRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Subject> findBy(String fieldName, Object value) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT s FROM Subject s WHERE s.").append(fieldName).append(" LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return em.createNativeQuery("SELECT id, name FROM subject ORDER BY name", "subjectNameResult").getResultList();
	}

}
