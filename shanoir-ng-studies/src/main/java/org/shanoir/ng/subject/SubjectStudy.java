package org.shanoir.ng.subject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.study.Study;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@JsonPropertyOrder({ "_links", "id" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class SubjectStudy extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 734032331139342460L;

	/** true if the subject is physically involved in the study. */
	private boolean physicallyInvolved;

	/** Subject yype. */
	@Column(nullable = true, updatable = true)
	@Enumerated(EnumType.STRING)
	private SubjectType subjectType;

	/** Study. */
	@ManyToOne
	@JoinColumn(name = "study")
	private Study study;

	/** Subject. */
	@ManyToOne
	@JoinColumn(name = "subject")
	private Subject subject;

	/** Identifier of the subject inside the study. */
	private String subjectStudyIdentifier;

	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
	public Long getId() {
		return super.getId();
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
