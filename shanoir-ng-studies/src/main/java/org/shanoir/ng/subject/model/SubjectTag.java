package org.shanoir.ng.subject.model;

import org.shanoir.ng.tag.model.Tag;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table
@IdClass(SubjectTagPk.class)
public class SubjectTag {

	@Id
	@ManyToOne
	@JoinColumn(name = "tag_id", insertable = false, updatable = false, nullable = false)
	private Tag tag;
	
	@Id
	@ManyToOne
	@JoinColumn(name = "subject_id", insertable = false, updatable = false, nullable = false)
	private Subject subject;

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

}