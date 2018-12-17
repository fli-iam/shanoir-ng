package org.shanoir.ng.studycenter;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * DTO for link between studies and centers.
 * 
 * @author msimon
 *
 */
public class StudyCenterDTO {

	private Long id;
	
	private IdNameDTO center;
	
	private IdNameDTO study;

	/** Investigator. */
	// private Long investigator_id;

	/** Investigator function in the study */
	// private InvestigatorFunction investigatorFunction;

	public IdNameDTO getStudy() {
		return study;
	}

	public void setStudy(IdNameDTO study) {
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
	public IdNameDTO getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(IdNameDTO center) {
		this.center = center;
	}
}
