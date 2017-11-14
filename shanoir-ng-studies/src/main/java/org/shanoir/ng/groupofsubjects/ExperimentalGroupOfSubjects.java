package org.shanoir.ng.groupofsubjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.study.Study;

/**
 * Experimental group of subjects.
 * 
 * @author msimon
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class ExperimentalGroupOfSubjects extends GroupOfSubjects {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -554912356059391766L;

	/** Study. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "study_id")
	@NotNull
	private Study study;

	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

	/**
	 * @param study
	 *            the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}

}
