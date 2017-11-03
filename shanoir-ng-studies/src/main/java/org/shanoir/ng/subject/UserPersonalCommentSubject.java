package org.shanoir.ng.subject;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@JsonPropertyOrder({ "_links", "id", "subject", "comment" })
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class UserPersonalCommentSubject extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 153755891423362269L;

	/** The subject. */
	@ManyToOne
	private Subject subject;

	/** The comment. */
	private String comment;

	/** The user. */
//	@ManyToOne
//	@JoinColumn(name = "user")
//	private User user;

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
