package org.shanoir.ng.subject.model;

import java.io.Serializable;

import org.shanoir.ng.tag.model.Tag;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class SubjectTagPk implements Serializable {
	
	private static final long serialVersionUID = -7148798112477845158L;
	
	@ManyToOne
	@JoinColumn(name = "tag_id", insertable = false, updatable = false, nullable = false)
	private Tag tag;
	
	@ManyToOne
	@JoinColumn(name = "subject_id", insertable = false, updatable = false, nullable = false)
	private Subject subject;

    public SubjectTagPk() {} 

    public SubjectTagPk(Tag tag, Subject subject) {
		super();
		this.tag = tag;
		this.subject = subject;
	}

	@Override
    public boolean equals(Object obj) {
    	return obj != null 
    			&& obj instanceof SubjectTagPk 
    			&& this.getSubject() != null
    			&& this.getTag() != null
    			&& ((SubjectTagPk) obj).getTag() != null
				&& ((SubjectTagPk) obj).getTag().getId() != null
    			&& ((SubjectTagPk) obj).getSubject() != null
				&& ((SubjectTagPk) obj).getSubject().getId() != null
    			&& this.getTag().getId().equals(((SubjectTagPk) obj).getTag().getId())
    			&& this.getSubject().getId().equals(((SubjectTagPk) obj).getSubject().getId());
    }
    
    @Override
    public int hashCode() {
    	return (int) getTag().hashCode() * (getSubject() != null ? getSubject().hashCode() : null);
    }

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
