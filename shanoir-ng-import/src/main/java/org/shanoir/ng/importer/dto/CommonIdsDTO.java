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

package org.shanoir.ng.importer.dto;

/**
 * DTO with center id, study id and subject id.
 * 
 * @author msimon
 *
 */
public class CommonIdsDTO {

	private Long centerId;

	private Long studyId;

	private Long subjectId;

	private Long equipementId;

	public CommonIdsDTO(Long centerId, Long studyId, Long subjectId, Long equipementId) {
		super();
		this.centerId = centerId;
		this.studyId = studyId;
		this.subjectId = subjectId;
		this.equipementId = equipementId;
	}

	/**
	 * @return the centerId
	 */
	public Long getCenterId() {
		return centerId;
	}

	/**
	 * @param centerId
	 *            the centerId to set
	 */
	public void setCenterId(Long centerId) {
		this.centerId = centerId;
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
	 * @return the subjectId
	 */
	public Long getSubjectId() {
		return subjectId;
	}

	/**
	 * @param subjectId
	 *            the subjectId to set
	 */
	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	/**
	 * @return the equipementId
	 */
	public Long getEquipementId() {
		return equipementId;
	}

	/**
	 * @param equipementId the equipementId to set
	 */
	public void setEquipementId(Long equipementId) {
		this.equipementId = equipementId;
	}
}
