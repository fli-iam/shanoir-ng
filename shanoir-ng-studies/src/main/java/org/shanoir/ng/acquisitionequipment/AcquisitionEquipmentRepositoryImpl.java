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

/**
 * 
 */
package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author yyao
 *
 */
public class AcquisitionEquipmentRepositoryImpl implements AcquisitionEquipmentRepositoryCustom {
	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<AcquisitionEquipment> findBy(String fieldName, Object value) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT a FROM AcquisitionEquipment a WHERE a.").append(fieldName).append(" LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}

	/**
	 * Find acquisition equipments by a couple of field value.
	 * 
	 * @param fieldName1
	 *            searched field name1.
	 * @param fieldName2
	 *            searched field name2.
	 * @param value1
	 *            value1.
	 * @param value2
	 *            value2.
	 * @author yyao
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AcquisitionEquipment> findByCoupleOfFieldValue(String fieldName1, Object value1, String fieldName2,
			Object value2) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT a FROM AcquisitionEquipment a WHERE a.").append(fieldName1)
				.append(" LIKE :value1 and a.").append(fieldName2).append(" LIKE :value2");
		return em.createQuery(sqlQuery.toString()).setParameter("value1", value1).setParameter("value2", value2)
				.getResultList();
	}

}
