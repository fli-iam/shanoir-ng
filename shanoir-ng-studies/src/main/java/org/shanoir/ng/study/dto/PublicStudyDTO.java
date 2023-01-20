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

package org.shanoir.ng.study.dto;

import org.shanoir.ng.profile.model.Profile;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.study.model.StudyStatus;
import org.shanoir.ng.study.model.StudyType;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.studycenter.StudyCenterDTO;
import org.shanoir.ng.subjectstudy.dto.SubjectStudyDTO;
import org.shanoir.ng.tag.model.StudyTagDTO;
import org.shanoir.ng.tag.model.TagDTO;
import org.shanoir.ng.timepoint.TimepointDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for study.
 * 
 * @author msimon
 *
 */
public class PublicStudyDTO {

	private boolean downloadableByDefault;

	@LocalDateAnnotations
	private LocalDate endDate;

	private Long id;

	private String name;

	private int nbExaminations;

	private int nbSubjects;

	@LocalDateAnnotations
	private LocalDate startDate;

	private StudyStatus studyStatus;

	private StudyType studyType;

	private String description;

	private List<StudyTagDTO> studyTags;

	/**
	 * Default constructor.
	 */
	public PublicStudyDTO() {
		// empty constructor
	}

	/**
	 * @return the downloadableByDefault
	 */
	public boolean isDownloadableByDefault() {
		return downloadableByDefault;
	}

	/**
	 * @param downloadableByDefault
	 *            the downloadableByDefault to set
	 */
	public void setDownloadableByDefault(boolean downloadableByDefault) {
		this.downloadableByDefault = downloadableByDefault;
	}

	/**
	 * @return the endDate
	 */
	public LocalDate getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the nbExaminations
	 */
	public int getNbExaminations() {
		return nbExaminations;
	}

	/**
	 * @param nbExaminations
	 *            the nbExaminations to set
	 */
	public void setNbExaminations(int nbExaminations) {
		this.nbExaminations = nbExaminations;
	}

	/**
	 * @return the nbSubjects
	 */
	public int getNbSubjects() {
		return nbSubjects;
	}

	/**
	 * @param nbSubjects
	 *            the nbSubjects to set
	 */
	public void setNbSubjects(int nbSubjects) {
		this.nbSubjects = nbSubjects;
	}

	/**
	 * @return the startDate
	 */
	public LocalDate getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the studyStatus
	 */
	public StudyStatus getStudyStatus() {
		return studyStatus;
	}

	/**
	 * @param studyStatus
	 *            the studyStatus to set
	 */
	public void setStudyStatus(StudyStatus studyStatus) {
		this.studyStatus = studyStatus;
	}

	/**
	 * @return the studyType
	 */
	public StudyType getStudyType() {
		return studyType;
	}

	/**
	 * @param studyType
	 *            the studyType to set
	 */
	public void setStudyType(StudyType studyType) {
		this.studyType = studyType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<StudyTagDTO> getStudyTags() {
		return studyTags;
	}

	public void setStudyTags(List<StudyTagDTO> studyTags) {
		this.studyTags = studyTags;
	}
}