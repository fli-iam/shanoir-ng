package org.shanoir.ng.study.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.core.model.IdName;
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
	public List<IdName> findIdsAndNames() {
		return em.createNativeQuery("SELECT id, name FROM study", "studyNameResult").getResultList();
	}

}
