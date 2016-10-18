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