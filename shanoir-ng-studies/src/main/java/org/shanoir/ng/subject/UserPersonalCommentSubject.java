package org.shanoir.ng.subject;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class UserPersonalCommentSubject implements Serializable, IDisplayableObject, IPersistableEntity{
	
	/** The Constant serialVersionUID. */
	//private static final long serialVersionUID = 3810595736081979394L;

	/** The subject. */
	@ManyToOne
	private Subject subject;

	/** The comment. */
	private String comment;

	/** The user. */
	/*@ManyToOne
	@JoinColumn(name = "user")
	private User user;*/

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.core.model.IDisplayableObject#getDisplayString()
	 */
	@Transient
	public String getDisplayString() {
		return getComment();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "" + getId();
		/*if (getUser() != null) {
			result += ", " + getUser().getId();
		}*/
		if (getSubject() != null) {
			result += ", " + getSubject().getId();
		}
		result += ", " + getComment();
		return result;
	}

	/**
	 * Gets the subject.
	 *
	 * @return the subject
	 */
	public Subject getSubject() {
		return subject;
	}

	/**
	 * Sets the subject.
	 *
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(final Subject subject) {
		this.subject = subject;
	}

	/**
	 * Gets the comment.
	 *
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Sets the comment.
	 *
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(final String comment) {
		this.comment = comment;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	/*public User getUser() {
		return user;
	}*/

	/**
	 * Sets the user.
	 *
	 * @param user
	 *            the user to set
	 */
	/*public void setUser(final User user) {
		this.user = user;
	}*/

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		///result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final UserPersonalCommentSubject other = (UserPersonalCommentSubject) obj;
		if (comment == null) {
			if (other.comment != null) {
				return false;
			}
		} else if (!comment.equals(other.comment)) {
			return false;
		}
		if (subject == null) {
			if (other.subject != null) {
				return false;
			}
		} else if (!subject.equals(other.subject)) {
			return false;
		}
		/*if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}*/
		return true;
	}

}
