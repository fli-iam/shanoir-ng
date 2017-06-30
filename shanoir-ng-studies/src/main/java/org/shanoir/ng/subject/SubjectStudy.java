package org.shanoir.ng.subject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.shanoir.ng.study.Study;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class SubjectStudy extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 734032331139342460L;


	/** true if the subject is physically involved in the study. */
	private boolean physicallyInvolved;

	/** Subject yype. */
	private Integer subjectType;

	/** Study. */
	@ManyToOne
	@JoinColumn(name = "study")
	private Study study;

	/** Subject. */
	@ManyToOne
	@JoinColumn(name = "subject",updatable = true, insertable = true)
	@JsonIgnore
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
		return SubjectType.getType(subjectType);
	}

	public void setSubjectType(SubjectType subjectType) {
		if (subjectType == null) {
			this.subjectType = null;
		} else {
			this.subjectType = subjectType.getId();
		}
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

	@Override
	public String toString(){
		return 	"[ID : " + this.getId() + ", "
						+ "physicallyInvolved : " + this.isPhysicallyInvolved() + ", "
				//		+ "subjectType : " + this.getSubjectType().getName() + ", "
			//			+ "study.id : " + this.getStudy().getId() + ", "
						+ "subjectStudyIdentifier : " + this.getSubjectStudyIdentifier() + "]";

	}
}
