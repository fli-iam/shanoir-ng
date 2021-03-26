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

package org.shanoir.ng.preclinical.extra_data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.springframework.stereotype.Component;

@Component
public class ExtraDataRepositoryImpl implements ExtraDataRepositoryCustom<ExaminationExtraData> {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<ExaminationExtraData> findBy(String fieldName, Object value) {
		return em.createQuery("SELECT ex FROM ExaminationExtraData ex WHERE ex." + fieldName + " LIKE :value")
				.setParameter("value", value).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExaminationExtraData> findAllByExaminationId(Long id) {
		return em.createQuery("SELECT ex FROM ExaminationExtraData ex WHERE ex.examinationId" + " LIKE :id")
				.setParameter("id", id).getResultList();
	}

}
