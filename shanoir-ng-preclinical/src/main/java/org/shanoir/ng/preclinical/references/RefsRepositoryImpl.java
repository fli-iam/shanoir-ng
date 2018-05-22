package org.shanoir.ng.preclinical.references;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

import org.shanoir.ng.preclinical.references.Reference;

@Component
public class RefsRepositoryImpl implements RefsRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Reference> findByCategory(String category) {
		return em.createQuery(
				"SELECT r FROM Reference r WHERE r.category LIKE :category")
				.setParameter("category", category)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Reference> findByCategoryAndType(String category,String reftype) {
		return em.createQuery(
				"SELECT r FROM Reference r WHERE r.category LIKE :category AND r.reftype LIKE :reftype")
				.setParameter("category", category)
				.setParameter("reftype", reftype)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Optional<Reference> findByCategoryTypeAndValue(String category, String reftype, String value) {
		List<Reference> resultList = em.createQuery(
				"SELECT r FROM Reference r WHERE r.category LIKE :category AND r.reftype LIKE :reftype AND r.value LIKE :value")
				.setParameter("category", category)
				.setParameter("reftype", reftype)
				.setParameter("value", value)
				.setMaxResults(1)
				.getResultList();
		if (resultList == null || resultList.isEmpty()) {
	        return Optional.empty();
	    }
	    return Optional.of(resultList.get(0));
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Optional<Reference> findByTypeAndValue(String reftype, String value) {
		List<Reference> resultList = em.createQuery(
				"SELECT r FROM Reference r WHERE r.reftype LIKE :reftype AND r.value LIKE :value")
				.setParameter("reftype", reftype)
				.setParameter("value", value)
				.setMaxResults(1)
				.getResultList();
		if (resultList == null || resultList.isEmpty()) {
	        return Optional.empty();
	    }
	    return Optional.of(resultList.get(0));
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> findCategories() {
		return em.createQuery(
				"SELECT DISTINCT(r.category) FROM Reference r")
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> findTypesByCategory(String category) {
		return em.createQuery(
				"SELECT DISTINCT(r.reftype) FROM Reference r WHERE r.category LIKE :category")
				.setParameter("category", category)
				.getResultList();
	}
	
	
}
