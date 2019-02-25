package org.shanoir.ng.study.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.study.model.Study;
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
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT s FROM Study s WHERE s.").append(fieldName).append(" LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return em.createNativeQuery("SELECT id, name FROM study", "studyNameResult").getResultList();
	}

}
