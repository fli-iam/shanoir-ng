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

package org.shanoir.ng.shared.validation;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.springframework.stereotype.Component;

/**
 * Custom repository for entities.
 * 
 * @author msimon
 *
 */
@Component
public class FindByRepositoryImpl<T extends AbstractEntity> implements FindByRepository<T> {

	@PersistenceContext
	private EntityManager em;


	@SuppressWarnings("unchecked")
	@Override
	public List<T> findBy(String fieldName, Object value, @SuppressWarnings("rawtypes") Class clazz) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT e FROM " + clazz.getSimpleName() + " e WHERE TRIM(e.").append(fieldName).append(") LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}

}
