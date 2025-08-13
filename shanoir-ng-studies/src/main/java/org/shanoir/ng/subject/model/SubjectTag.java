package org.shanoir.ng.subject.model;

import org.shanoir.ng.tag.model.Tag;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(SubjectTagPrimaryKey.class)
public class SubjectTag {

	@Id
	@ManyToOne
	@JoinColumn(name = "subject_id")
	private Subject subject;

	@Id
	@ManyToOne
	@JoinColumn(name = "tag_id")
	private Tag tag;	

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

}