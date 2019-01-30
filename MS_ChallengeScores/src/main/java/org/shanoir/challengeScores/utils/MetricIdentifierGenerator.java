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

package org.shanoir.challengeScores.utils;

import java.io.Serializable;
import java.math.BigInteger;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.shanoir.challengeScores.data.model.Metric;
import org.springframework.stereotype.Component;

@Component
public class MetricIdentifierGenerator implements IdentifierGenerator {

	public Serializable generate(SessionImplementor si, Object entity) {
		Metric myEntity = (Metric) entity;
		if (myEntity.getId() != null && myEntity.getId() > 0) {
			// the identifier has been set manually => use it
			return myEntity.getId();
		} else {
			// the identifier is not provided => generate it
			Long lastId = ((BigInteger) si.getNamedQuery("getLastId").uniqueResult()).longValue();
			return lastId + 1;
		}
	}

}