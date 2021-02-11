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

package org.shanoir.ng.study.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PostLoad;
import javax.persistence.SqlResultSetMapping;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.shanoir.ng.groupofsubjects.ExperimentalGroupOfSubjects;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.security.EditableOnlyBy;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.timepoint.Timepoint;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Study.
 * 
 * @author msimon
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
@SqlResultSetMapping(name = "studyNameResult", classes = { @ConstructorResult(targetClass = IdName.class, columns = {
		@ColumnResult(name = "id", type = Long.class), @ColumnResult(name = "name") }) })
@JsonIdentityInfo(scope=Study.class, generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Study extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 2182323766659913794L;

	/** Is clinical. */
	@NotNull
	private boolean clinical;

	/** Coordinator. */
	private Long coordinatorId;

	/** Is with downloadable by default. */
	private boolean downloadableByDefault;

	/** End date. */
	@LocalDateAnnotations
	private LocalDate endDate;

	/** List of the examinations related to this study. */
	@ElementCollection
	@CollectionTable(name = "study_examination")
	@Column(name = "examination_id")
	private Set<Long> examinationIds;

	/** Associated experimental groups of subjects. */
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, orphanRemoval = true)
	private List<ExperimentalGroupOfSubjects> experimentalGroupsOfSubjects;

	/** The is mono center. */
	@NotNull
	private boolean monoCenter;

	@NotBlank
	@Column(unique = true)
	@Unique
	@EditableOnlyBy(roles = { "ROLE_ADMIN", "ROLE_EXPERT" })
	private String name;

	/** List of protocol files directly attached to the study. */
	@ElementCollection
	@CollectionTable(name = "protocol_file_path")
	@Column(name = "path")
	private List<String> protocolFilePaths;

	/** Start date. */
	@LocalDateAnnotations
	private LocalDate startDate;

	/** Relations between the investigators, the centers and the studies. */
	@NotEmpty
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StudyCenter> studyCenterList;

	@NotNull
	private Integer studyStatus;
	
	private Integer studyType;

	/** Users associated to the research study. */
	@OneToMany(mappedBy = "study", targetEntity = StudyUser.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StudyUser> studyUserList;

	/** Relations between the subjects and the studies. */
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SubjectStudy> subjectStudyList;

	/** List of Timepoints dividing the study **/
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("rank asc")
	private List<Timepoint> timepoints;
	
	/** Is visible by default. */
	private boolean visibleByDefault;

	/** Is with examination. */
	private boolean withExamination;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "study/" + getId());
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
	 * @return the coordinatorId
	 */
	public Long getCoordinatorId() {
		return coordinatorId;
	}

	/**
	 * @param coordinatorId
	 *            the coordinatorId to set
	 */
	public void setCoordinatorId(Long coordinatorId) {
		this.coordinatorId = coordinatorId;
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
	 * @return the examinationIds
	 */
	public Set<Long> getExaminationIds() {
		return examinationIds;
	}

	/**
	 * @param examinationIds
	 *            the examinationIds to set
	 */
	public void setExaminationIds(Set<Long> examinationIds) {
		this.examinationIds = examinationIds;
	}

	/**
	 * @return the experimentalGroupsOfSubjects
	 */
	public List<ExperimentalGroupOfSubjects> getExperimentalGroupsOfSubjects() {
		return experimentalGroupsOfSubjects;
	}

	/**
	 * @param experimentalGroupsOfSubjects
	 *            the experimentalGroupsOfSubjects to set
	 */
	public void setExperimentalGroupsOfSubjects(List<ExperimentalGroupOfSubjects> experimentalGroupsOfSubjects) {
		this.experimentalGroupsOfSubjects = experimentalGroupsOfSubjects;
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
	 * @return the studyCenterList
	 */
	public List<StudyCenter> getStudyCenterList() {
		return studyCenterList;
	}

	/**
	 * @param studyCenterList
	 *            the studyCenterList to set
	 */
	public void setStudyCenterList(List<StudyCenter> studyCenterList) {
		this.studyCenterList = studyCenterList;
	}

	/**
	 * @return the studyStatus
	 */
	public StudyStatus getStudyStatus() {
		return StudyStatus.getStatus(studyStatus);
	}

	/**
	 * @param studyStatus
	 *            the studyStatus to set
	 */
	public void setStudyStatus(StudyStatus studyStatus) {
		if (studyStatus != null) {
			this.studyStatus = studyStatus.getId();
		}
	}

	/**
	 * @return the studyType
	 */
	public StudyType getStudyType() {
		return StudyType.getType(studyType);
	}

	/**
	 * @param studyType
	 *            the studyType to set
	 */
	public void setStudyType(StudyType studyType) {
		if (studyType == null) {
			this.studyType = null;
		} else {
			this.studyType = studyType.getId();
		}
	}

	/**
	 * @return the studyUserList
	 */
	public List<StudyUser> getStudyUserList() {
		return studyUserList;
	}

	/**
	 * @param studyUserList
	 *            the studyUserList to set
	 */
	public void setStudyUserList(List<StudyUser> studyUserList) {
		this.studyUserList = studyUserList;
	}

	/**
	 * @return the subjectStudyList
	 */
	public List<SubjectStudy> getSubjectStudyList() {
		return subjectStudyList;
	}

	/**
	 * @param subjectStudyList
	 *            the subjectStudyList to set
	 */
	public void setSubjectStudyList(List<SubjectStudy> subjectStudyList) {
		this.subjectStudyList = subjectStudyList;
	}

	/**
	 * @return the timepoints
	 */
	public List<Timepoint> getTimepoints() {
		return timepoints;
	}

	/**
	 * @param timepoints the timepoints to set
	 */
	public void setTimepoints(List<Timepoint> timepoints) {
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
}
