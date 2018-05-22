package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.preclinical.references.Reference;
import org.springframework.stereotype.Component;

@Component
public class AnimalSubjectRepositoryImpl implements AnimalSubjectRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<AnimalSubject> findByReference(Reference reference) {
		return em.createQuery(
				"SELECT a FROM AnimalSubject a LEFT JOIN a." + reference.getReftype() + " r WHERE r.value LIKE :value")
				.setParameter("value", reference.getValue()).getResultList();
	}

}
