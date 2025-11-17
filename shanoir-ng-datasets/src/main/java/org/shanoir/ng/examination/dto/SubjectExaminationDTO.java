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

package org.shanoir.ng.examination.dto;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

/**
 * Simple examination DTO with information for subject.
 * 
 * @author msimon
 *
 */
public class SubjectExaminationDTO {

	private String comment;

	@LocalDateAnnotations
	private LocalDate examinationDate;

	private Long id;
	
	private List<String> extraDataFilePathList;
	
	private Long centerId;
	
	private Long studyId;

	private boolean preclinical;

	private String studyInstanceUID;

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the examinationDate
	 */
	public LocalDate getExaminationDate() {
		return examinationDate;
	}

	/**
	 * @param examinationDate
	 *            the examinationDate to set
	 */
	public void setExaminationDate(LocalDate examinationDate) {
		this.examinationDate = examinationDate;
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
	
	public List<String> getExtraDataFilePathList() {
		return extraDataFilePathList;
	}

	public void setExtraDataFilePathList(final List<String> extraDataFilePathList) {
		this.extraDataFilePathList = extraDataFilePathList;
	}

	public Long getCenterId() {
		return centerId;
	}

	public void setCenterId(Long centerId) {
		this.centerId = centerId;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public boolean getPreclinical() {
		return preclinical;
	}

	public void setPreclinical(boolean preclinical) {
		this.preclinical = preclinical;
	}

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

}
