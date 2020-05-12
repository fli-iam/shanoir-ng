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

package org.shanoir.ng.subject.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.shanoir.ng.subject.model.Subject;
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

	@Override
	public Subject findSubjectWithSubjectStudyById(Long id) {
		Query q = em.createQuery("SELECT s FROM Subject s LEFT JOIN FETCH s.subjectStudyList where s.id=:id",Subject.class);
		q.setParameter("id", id);
		return (Subject) q.getSingleResult();
	}

}
