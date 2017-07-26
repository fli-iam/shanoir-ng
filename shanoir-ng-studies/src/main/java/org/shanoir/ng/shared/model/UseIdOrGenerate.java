package org.shanoir.ng.shared.model;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentityGenerator;

public class UseIdOrGenerate extends IdentityGenerator {

	@SuppressWarnings("rawtypes")
	@Override
	public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
		if (obj == null)
			throw new HibernateException(new NullPointerException());

		if (!(obj instanceof Identifiable))
			throw new HibernateException("Object is not a Identifiable. No Id generation");

		if ((((Identifiable) obj).getId()) == null) {
			Serializable id = super.generate(session, obj);
			return id;
		} else {
			return ((Identifiable) obj).getId();
		}
	}
}
