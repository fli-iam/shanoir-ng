package org.shanoir.ng.study;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.center.Center;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Link between studies and centers.
 * 
 * @author msimon
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class StudyCenter extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 1007750133610651645L;

	/** The study. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "studyId")
	private Study study;

	/** Center. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "centerId")
	private Center center;

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

}
