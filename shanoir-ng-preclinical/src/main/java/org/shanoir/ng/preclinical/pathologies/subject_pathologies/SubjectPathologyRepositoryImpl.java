package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.references.Reference;
import org.springframework.stereotype.Component;


@Component
public class SubjectPathologyRepositoryImpl implements SubjectPathologyRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
		
	@SuppressWarnings("unchecked")
	@Override
	public List<SubjectPathology> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT p FROM SubjectPathology p WHERE p." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}
	
	/*@SuppressWarnings("unchecked")
	@Override
	public List<SubjectPathology> findAllBySubject(Subject subject) {
		return em.createQuery(
				"SELECT p FROM SubjectPathology p WHERE p.subject LIKE :subject")
				.setParameter("subject", subject)
				.getResultList();
	}*/
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SubjectPathology> findAllByPathology(Pathology pathology) {
		return em.createQuery(
				"SELECT p FROM SubjectPathology p WHERE p.pathology LIKE :pathology")
				.setParameter("pathology", pathology)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SubjectPathology> findAllByPathologyModel(PathologyModel model) {
		return em.createQuery(
				"SELECT p FROM SubjectPathology p WHERE p.pathologyModel LIKE :model")
				.setParameter("model", model)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SubjectPathology> findAllByLocation(Reference location) {
		return em.createQuery(
				"SELECT p FROM SubjectPathology p WHERE p.location LIKE :location")
				.setParameter("location", location)
				.getResultList();
	}
}
