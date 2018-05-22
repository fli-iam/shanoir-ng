package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.springframework.stereotype.Component;

@Component
public class SubjectTherapyRepositoryImpl implements SubjectTherapyRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<SubjectTherapy> findBy(String fieldName, Object value) {
		return em.createQuery("SELECT t FROM SubjectTherapy t WHERE t." + fieldName + " LIKE :value")
				.setParameter("value", value).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SubjectTherapy> findByAnimalSubject(AnimalSubject animalSubject) {
		return em.createQuery("SELECT t FROM SubjectTherapy t WHERE t.animalSubject LIKE :animalSubject")
				.setParameter("animalSubject", animalSubject).getResultList();
	}

}
