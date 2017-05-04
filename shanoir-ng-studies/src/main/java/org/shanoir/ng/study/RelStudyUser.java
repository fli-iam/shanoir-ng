package org.shanoir.ng.study;

import javax.persistence.Column;

/**
 * Relation between the study and the users.
 *
 * @author ifakhfak
 */
public class RelStudyUser {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -6816811624812002519L;

	/** ID. */
	private Long id;

	/** Type of the relationship. */
	/*
	 * @ManyToOne(fetch = FetchType.EAGER)
	 * 
	 * @JoinColumn(name = "REF_STUDY_USER_TYPE_ID", nullable = false) private
	 * RefStudyUserType refStudyUserType;
	 */

	/** Study. */
	private Study study;

	/** User. */
	@Column(name = "USER_ID")
	private Long userId;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	/*
	 * public int compareTo(final RelStudyUser other) { return new
	 * Long(this.getId()).compareTo(new Long(other.id)); }
	 */

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.core.model.IPersistableEntity#getId()
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Gets the ref study user type.
	 *
	 * @return the refStudyUserType
	 */
	// @XmlJavaTypeAdapter(RefStudyUserTypeXmlAdapter.class)
	/*
	 * public RefStudyUserType getRefStudyUserType() { return refStudyUserType;
	 * }
	 */

	/**
	 * Gets the study.
	 *
	 * @return the study
	 */
	// @XmlTransient
	public Study getStudy() {
		return study;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public Long getUser() {
		return userId;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * Sets the ref study user type.
	 *
	 * @param refStudyUserType
	 *            the refStudyUserType to set
	 */
	/*
	 * public void setRefStudyUserType(final RefStudyUserType refStudyUserType)
	 * { this.refStudyUserType = refStudyUserType; }
	 */

	/**
	 * Sets the study.
	 *
	 * @param study
	 *            the study to set
	 */
	public void setStudy(final Study study) {
		this.study = study;
	}

	/**
	 * Sets the user.
	 *
	 * @param user
	 *            the user to set
	 */
	public void setUser(final Long userId) {
		this.userId = userId;
	}

}
