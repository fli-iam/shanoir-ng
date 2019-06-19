package org.shanoir.ng.studycenter;

import org.shanoir.ng.shared.core.model.IdName;

/**
 * DTO for link between studies and centers.
 * 
 * @author msimon
 *
 */
public class StudyCenterDTO {

	private Long id;
	
	private IdName center;
	
	private IdName study;

	/** Investigator. */
	// private Long investigator_id;

	/** Investigator function in the study */
	// private InvestigatorFunction investigatorFunction;

	public IdName getStudy() {
		return study;
	}

	public void setStudy(IdName study) {
		this.study = study;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the center
	 */
	public IdName getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(IdName center) {
		this.center = center;
	}
}
