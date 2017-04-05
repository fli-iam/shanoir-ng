package org.shanoir.ng.shared.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Generic class used to manage entities common data.
 *
 * @author msimon
 *
 */
@MappedSuperclass
public abstract class AbstractGenericItem implements Identifiable<Long>, Serializable {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 6844259659282870007L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
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

}
