package org.shanoir.ng.studycenter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.study.model.Study;

/**
 * Link between studies and centers.
 * 
 * @author msimon
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class StudyCenter extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 1007750133610651645L;

	/** Center. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "center_id")
	private Center center;

	/** Investigator. */
//	@ManyToOne
//	@JoinColumn(name = "investigator_id")
//	private Investigator investigator;

	/** Investigator function in the study */
//	private InvestigatorFunction investigatorFunction;

	/** The study. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "study_id")
	private Study study;
	
	/**
	 * @return the center
	 */
	public Center getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter(Center center) {
		this.center = center;
	}

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
