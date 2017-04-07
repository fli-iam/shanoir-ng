package org.shanoir.ng.subject;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.study.Study;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "subject_study")
public class SubjectStudy{
	
	@Id
	private Long id;
	
	private boolean physicallyInvolved;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type")
	private SubjectType subjectType;
	
	/** Study. */
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "study")
	private Study study;
	
	/** Subject. */
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "subject")
	private Subject subject;
	
	
	private String subjectStudyIdentifier;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isPhysicallyInvolved() {
		return physicallyInvolved;
	}

	public void setPhysicallyInvolved(boolean physicallyInvolved) {
		this.physicallyInvolved = physicallyInvolved;
	}

	public SubjectType getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(SubjectType subjectType) {
		this.subjectType = subjectType;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public String getSubjectStudyIdentifier() {
		return subjectStudyIdentifier;
	}

	public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
		this.subjectStudyIdentifier = subjectStudyIdentifier;
	}
	
	
}
