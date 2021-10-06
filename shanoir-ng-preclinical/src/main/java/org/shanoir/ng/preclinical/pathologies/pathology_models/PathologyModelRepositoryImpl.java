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

package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.springframework.stereotype.Component;


@Component
public class PathologyModelRepositoryImpl implements PathologyModelRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PathologyModel> findByPathology(Pathology pathology) {
		return em.createQuery(
				"SELECT pm FROM PathologyModel pm LEFT JOIN pm.pathology p WHERE p.id = :id")
				.setParameter("id", pathology.getId())
				.getResultList();
	}
}
