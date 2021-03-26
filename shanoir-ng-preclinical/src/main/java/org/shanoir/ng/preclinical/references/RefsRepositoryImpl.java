/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.preclinical.references;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

@Component
public class RefsRepositoryImpl implements RefsRepositoryCustom{

	private static final String CATEGORY2 = "category";
	private static final String REFTYPE2 = "reftype";
	@PersistenceContext
    private EntityManager em;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Reference> findByCategory(String category) {
		return em.createQuery(
				"SELECT r FROM Reference r WHERE r.category LIKE :category")
				.setParameter(CATEGORY2, category)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Reference> findByCategoryAndType(String category,String reftype) {
		return em.createQuery(
				"SELECT r FROM Reference r WHERE r.category LIKE :category AND r.reftype LIKE :reftype")
				.setParameter(CATEGORY2, category)
				.setParameter(REFTYPE2, reftype)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Optional<Reference> findByCategoryTypeAndValue(String category, String reftype, String value) {
		List<Reference> resultList = em.createQuery(
				"SELECT r FROM Reference r WHERE r.category LIKE :category AND r.reftype LIKE :reftype AND r.value LIKE :value")
				.setParameter(CATEGORY2, category)
				.setParameter(REFTYPE2, reftype)
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
				.setParameter(REFTYPE2, reftype)
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
				.setParameter(CATEGORY2, category)
				.getResultList();
	}
	
	
}
