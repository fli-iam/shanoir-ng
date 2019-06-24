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

package org.shanoir.ng.studycard.dto;

/**
 * DTO for link between a study card and a study.
 * 
 * @author msimon
 *
 */
public class StudyStudyCardDTO {

	// Create new link to study
	private Long newStudyId;

	// Delete old link to study
	private Long oldStudyId;

	private Long studyCardId;

	/**
	 * Simple constructor.
	 */
	public StudyStudyCardDTO() {
	}

	/**
	 * Constructor.
	 * 
	 * @param studyCardId
	 *            study card id.
	 * @param newStudyId
	 *            study id for link creation.
	 * @param oldStudyId
	 *            study id for link deletion.
	 */
	public StudyStudyCardDTO(final Long studyCardId, final Long newStudyId, final Long oldStudyId) {
		this.studyCardId = studyCardId;
		this.newStudyId = newStudyId;
		this.oldStudyId = oldStudyId;
	}

	/**
	 * @return the newStudyId
	 */
	public Long getNewStudyId() {
		return newStudyId;
	}

	/**
	 * @param newStudyId
	 *            the newStudyId to set
	 */
	public void setNewStudyId(Long newStudyId) {
		this.newStudyId = newStudyId;
	}

	/**
	 * @return the oldStudyId
	 */
	public Long getOldStudyId() {
		return oldStudyId;
	}

	/**
	 * @param oldStudyId
	 *            the oldStudyId to set
	 */
	public void setOldStudyId(Long oldStudyId) {
		this.oldStudyId = oldStudyId;
	}

	/**
	 * @return the studyCardId
	 */
	public Long getStudyCardId() {
		return studyCardId;
	}

	/**
	 * @param studyCardId
	 *            the studyCardId to set
	 */
	public void setStudyCardId(Long studyCardId) {
		this.studyCardId = studyCardId;
	}

}
