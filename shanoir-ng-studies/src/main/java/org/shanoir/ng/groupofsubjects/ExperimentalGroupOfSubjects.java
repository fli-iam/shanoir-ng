package org.shanoir.ng.groupofsubjects;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.study.Study;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Experimental group of subjects.
 * 
 * @author msimon
 *
 */
@Entity
@DiscriminatorValue("EXPERIMENTAL")
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class ExperimentalGroupOfSubjects extends GroupOfSubjects {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -554912356059391766L;

	/** Study. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "study_id")
	@NotNull
	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
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
