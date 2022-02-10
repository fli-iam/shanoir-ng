package org.shanoir.ng.studyexamination;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.subject.model.Subject;

/**
 * This class is the link between a study and an examination.
 * It also supports linked center and subject.
 * @author jcome
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class StudyExamination extends AbstractEntity {

	private static final long serialVersionUID = -6040639164236575228L;

	private Long examinationId;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "study_id")
	private Study study;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "center_id")
	private Center center;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "subject_id")
	private Subject subject;

	public StudyExamination() {
		// default constructor
	}

	public StudyExamination(Long examinationId, Study study, Center center, Subject subject) {
		super();
		this.examinationId = examinationId;
		this.study = study;
		this.center = center;
		this.subject = subject;
	}

	/**
	 * @return the examinationId
	 */
	public Long getExaminationId() {
		return examinationId;
	}

	/**
	 * @param examinationId the examinationId to set
	 */
	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
	}

	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}

	/**
	 * @return the center
	 */
	public Center getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(Center center) {
		this.center = center;
	}

	/**
	 * @return the subject
	 */
	public Subject getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Subject subject) {
		this.subject = subject;
	}
	
}
