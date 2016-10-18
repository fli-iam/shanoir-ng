package org.shanoir.challengeScores.data.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.shanoir.challengeScores.utils.Utils;

/**
 * Shanoir Patient (Subject)
 *
 * @author jlouis
 */
@Entity
public class Patient {

	@Id
	private Long id = null;

	private String name;


	/**
	 * Constructor
	 */
	public Patient() {

	}


	/**
	 * Constructor
	 */
	public Patient(Long id) {
		this.id = id;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Patient) {
			Patient other = (Patient) obj;
			if (this.getId() == null) {
				return other.getId() == null && Utils.equals(this.getName(), other.getName());
			} else {
				return this.getId().equals(other.getId());
			}
		} else {
			return false;
		}
	}


	@Override
	public int hashCode() {
		if (id != null) {
			return id.hashCode();
		} else {
			return name.hashCode();
		}
	}


	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
