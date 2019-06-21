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

package org.shanoir.ng.shared.core.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Generic class used to manage entities common data.
 * 
 * @author msimon
 *
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -3276989363792089822L;

	@Id
	@GeneratedValue
	private Long id;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (obj == null) return false;
//		if (!(obj instanceof AbstractEntity)) return false;
//		AbstractEntity entity = (AbstractEntity) obj;
//		if (this.getId() == null && entity.getId() != null) return false;
//		else return this.getId().equals(entity.getId());
//	}
//	
//	@Override
//	public int hashCode() {
//		return 31;
//	}
}
