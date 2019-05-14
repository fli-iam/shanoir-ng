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
