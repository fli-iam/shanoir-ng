package org.shanoir.ng.studycenter;

import org.shanoir.ng.center.CenterDTO;

/**
 * DTO for link between studies and centers.
 * 
 * @author msimon
 *
 */
public class StudyCenterDTO {

	private CenterDTO center;

	private Long id;

	/** Investigator. */
	// private Long investigator_id;

	/** Investigator function in the study */
	// private InvestigatorFunction investigatorFunction;

	private Long studyId;
	
	private Boolean compatible = false;

	/**
	 * @return the center
	 */
	public CenterDTO getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter(CenterDTO center) {
		this.center = center;
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
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId
	 *            the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the compatible
	 */
	public Boolean getCompatible() {
		return compatible;
	}

	/**
	 * @param compatible the compatible to set
	 */
	public void setCompatible(Boolean compatible) {
		this.compatible = compatible;
	}

}
