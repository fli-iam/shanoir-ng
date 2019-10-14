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

package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.preclinical.references.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AnimalSubjectRepositoryImpl implements AnimalSubjectRepositoryCustom {

	private static final Logger LOG = LoggerFactory.getLogger(AnimalSubjectRepositoryImpl.class);

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<AnimalSubject> findByReference(Reference reference) {
		return em.createQuery(
				"SELECT a FROM AnimalSubject a LEFT JOIN a." + reference.getReftype() + " r WHERE r.value LIKE :value")
				.setParameter("value", reference.getValue()).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnimalSubject> findBy(String fieldName, Object value) {
		return em.createQuery("SELECT a FROM AnimalSubject a WHERE a." + fieldName + " LIKE :value")
				.setParameter("value", value).getResultList();
	}

}
