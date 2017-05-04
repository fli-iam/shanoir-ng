package org.shanoir.ng.subject;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.study.Study;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@Entity
@Table(name = "subject_study")
@JsonPropertyOrder({ "_links", "id", "subject", "subjectType", "study", "subjectStudyIdentifier" , "physicallyInvolved" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class SubjectStudy extends HalEntity{


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
	@JoinColumn(name = "subject",updatable = true, insertable = true)
	private Subject subject;


	private String subjectStudyIdentifier;


	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
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

	@Override
	public String toString(){
		return 	"[ID : " + this.getId() + ", "
						+ "physicallyInvolved : " + this.isPhysicallyInvolved() + ", "
				//		+ "subjectType : " + this.getSubjectType().getName() + ", "
			//			+ "study.id : " + this.getStudy().getId() + ", "
						+ "subjectStudyIdentifier : " + this.getSubjectStudyIdentifier() + "]";

	}


}
