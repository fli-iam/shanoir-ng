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

package org.shanoir.ng.study;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.studycenter.StudyCenterDTO;
import org.shanoir.ng.studyuser.StudyUser;
import org.shanoir.ng.subjectstudy.SubjectStudyDTO;
import org.shanoir.ng.timepoint.TimepointDTO;

/**
 * DTO for study.
 * 
 * @author msimon
 *
 */
public class StudyDTO {

	private boolean clinical;

	private boolean downloadableByDefault;

	@LocalDateAnnotations
	private LocalDate endDate;

	private List<IdNameDTO> experimentalGroupsOfSubjects;

	private Long id;

	private List<MembersCategoryDTO> membersCategories;

	private boolean monoCenter;

	private String name;

	private int nbExaminations;

	private int nbSujects;

	private List<String> protocolFilePaths;

	@LocalDateAnnotations
	private LocalDate startDate;

	private List<IdNameDTO> studyCards;

	private List<StudyCenterDTO> studyCenterList;

	private StudyStatus studyStatus;

	private StudyType studyType;

	private List<SubjectStudyDTO> subjectStudyList;

	private List<TimepointDTO> timepoints;

	private boolean visibleByDefault;

	private boolean withExamination;
	
	private List<StudyUser> studyUserList;

	/**
	 * Default constructor.
	 */
	public StudyDTO() {
	}

	/**
	 * @return the clinical
	 */
	public boolean isClinical() {
		return clinical;
	}

	/**
	 * @param clinical
	 *            the clinical to set
	 */
	public void setClinical(boolean clinical) {
		this.clinical = clinical;
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
	 * @return the experimentalGroupsOfSubjects
	 */
	public List<IdNameDTO> getExperimentalGroupsOfSubjects() {
		return experimentalGroupsOfSubjects;
	}

	/**
	 * @param experimentalGroupsOfSubjects
	 *            the experimentalGroupsOfSubjects to set
	 */
	public void setExperimentalGroupsOfSubjects(List<IdNameDTO> experimentalGroupsOfSubjects) {
		this.experimentalGroupsOfSubjects = experimentalGroupsOfSubjects;
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
	 * @return the membersCategories
	 */
	public List<MembersCategoryDTO> getMembersCategories() {
		return membersCategories;
	}

	/**
	 * @param membersCategories
	 *            the membersCategories to set
	 */
	public void setMembersCategories(List<MembersCategoryDTO> membersCategories) {
		this.membersCategories = membersCategories;
	}

	/**
	 * @return the monoCenter
	 */
	public boolean isMonoCenter() {
		return monoCenter;
	}

	/**
	 * @param monoCenter
	 *            the monoCenter to set
	 */
	public void setMonoCenter(boolean monoCenter) {
		this.monoCenter = monoCenter;
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
	 * @return the nbSujects
	 */
	public int getNbSujects() {
		return nbSujects;
	}

	/**
	 * @param nbSujects
	 *            the nbSujects to set
	 */
	public void setNbSujects(int nbSujects) {
		this.nbSujects = nbSujects;
	}

	/**
	 * @return the protocolFilePaths
	 */
	public List<String> getProtocolFilePaths() {
		return protocolFilePaths;
	}

	/**
	 * @param protocolFilePaths
	 *            the protocolFilePaths to set
	 */
	public void setProtocolFilePaths(List<String> protocolFilePaths) {
		this.protocolFilePaths = protocolFilePaths;
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
	 * @return the studyCards
	 */
	public List<IdNameDTO> getStudyCards() {
		return studyCards;
	}

	/**
	 * @param studyCards
	 *            the studyCards to set
	 */
	public void setStudyCards(List<IdNameDTO> studyCards) {
		this.studyCards = studyCards;
	}

	/**
	 * @return the studyCenterList
	 */
	public List<StudyCenterDTO> getStudyCenterList() {
		return studyCenterList;
	}

	/**
	 * @param studyCenterList
	 *            the studyCenterList to set
	 */
	public void setStudyCenterList(List<StudyCenterDTO> studyCenterList) {
		this.studyCenterList = studyCenterList;
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

	/**
	 * @return the subjectStudyList
	 */
	public List<SubjectStudyDTO> getSubjectStudyList() {
		return subjectStudyList;
	}

	/**
	 * @param subjectStudyList
	 *            the subjectStudyList to set
	 */
	public void setSubjectStudyList(List<SubjectStudyDTO> subjectStudyList) {
		this.subjectStudyList = subjectStudyList;
	}

	/**
	 * @return the timepoints
	 */
	public List<TimepointDTO> getTimepoints() {
		return timepoints;
	}

	/**
	 * @param timepoints
	 *            the timepoints to set
	 */
	public void setTimepoints(List<TimepointDTO> timepoints) {
		this.timepoints = timepoints;
	}

	/**
	 * @return the visibleByDefault
	 */
	public boolean isVisibleByDefault() {
		return visibleByDefault;
	}

	/**
	 * @param visibleByDefault
	 *            the visibleByDefault to set
	 */
	public void setVisibleByDefault(boolean visibleByDefault) {
		this.visibleByDefault = visibleByDefault;
	}

	/**
	 * @return the withExamination
	 */
	public boolean isWithExamination() {
		return withExamination;
	}

	/**
	 * @param withExamination
	 *            the withExamination to set
	 */
	public void setWithExamination(boolean withExamination) {
		this.withExamination = withExamination;
	}

	/**
	 * @return the studyUserList
	 */
	public List<StudyUser> getStudyUserList() {
		return studyUserList;
	}

	/**
	 * @param studyUserList the studyUserList to set
	 */
	public void setStudyUserList(List<StudyUser> studyUserList) {
		this.studyUserList = studyUserList;
	}

}
