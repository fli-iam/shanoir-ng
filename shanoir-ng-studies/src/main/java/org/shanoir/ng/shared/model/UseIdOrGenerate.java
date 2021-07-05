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

package org.shanoir.ng.shared.model;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentityGenerator;

public class UseIdOrGenerate extends IdentityGenerator {

	@SuppressWarnings("rawtypes")
	public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
		if (obj == null) {
			throw new HibernateException(new NullPointerException());
		}

		if (!(obj instanceof Identifiable)) {
			throw new HibernateException("Object is not a Identifiable. No Id generation");
		}

		if (((Identifiable) obj).getId() == null) {
			return super.generate(session, obj);
		} else {
			return ((Identifiable) obj).getId();
		}
	}
}
