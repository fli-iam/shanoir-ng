/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
