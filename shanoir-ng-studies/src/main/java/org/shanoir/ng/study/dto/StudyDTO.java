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

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.profile.model.Profile;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.study.model.StudyStatus;
import org.shanoir.ng.study.model.StudyType;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.studycenter.StudyCenterDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subjectstudy.dto.SubjectStudyDTO;
import org.shanoir.ng.tag.model.StudyTagDTO;
import org.shanoir.ng.tag.model.TagDTO;
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

    private List<IdName> experimentalGroupsOfSubjects;

    private Long id;

    private String name;

    private Profile profile;

    private int nbExaminations;

    private int nbSubjects;

    private List<String> protocolFilePaths;

    private List<String> dataUserAgreementPaths;

    private List<TagDTO> tags;

    private List<StudyTagDTO> studyTags;

    @LocalDateAnnotations
    private LocalDate startDate;

    private List<IdName> studyCards;

    private List<StudyCenterDTO> studyCenterList;

    private StudyStatus studyStatus;

    private StudyType studyType;

    private List<SubjectStudyDTO> subjectStudyList;

    private List<SubjectDTO> subjects;

    private List<TimepointDTO> timepoints;

    private boolean visibleByDefault;

    private boolean withExamination;

    private String studyCardPolicy;

    private List<StudyUser> studyUserList;

    private boolean challenge;

    private String description;

    private String license;

    private StudyStorageVolumeDTO storageVolume;

    /**
     * Default constructor.
     */
    public StudyDTO() {
        // empty constructor
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
    public List<IdName> getExperimentalGroupsOfSubjects() {
        return experimentalGroupsOfSubjects;
    }

    /**
     * @param experimentalGroupsOfSubjects
     *            the experimentalGroupsOfSubjects to set
     */
    public void setExperimentalGroupsOfSubjects(List<IdName> experimentalGroupsOfSubjects) {
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
     * @return the profileName
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * @param profile
     *            the profileName to set
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
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
    public int getNbSubjects() {
        return nbSubjects;
    }

    /**
     * @param nbSujects
     *            the nbSujects to set
     */
    public void setNbSubjects(int nbSubjects) {
        this.nbSubjects = nbSubjects;
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
    public List<IdName> getStudyCards() {
        return studyCards;
    }

    /**
     * @param studyCards
     *            the studyCards to set
     */
    public void setStudyCards(List<IdName> studyCards) {
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

    public List<SubjectDTO> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectDTO> subjectList) {
        this.subjects = subjectList;
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

    public String getStudyCardPolicy() {
        return studyCardPolicy;
    }

    public void setStudyCardPolicy(String studyCardPolicy) {
        this.studyCardPolicy = studyCardPolicy;
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

    /**
     * @return the dataUserAgreementPaths
     */
    public List<String> getDataUserAgreementPaths() {
        return dataUserAgreementPaths;
    }

    /**
     * @param dataUserAgreementPaths the dataUserAgreementPaths to set
     */
    public void setDataUserAgreementPaths(List<String> dataUserAgreementPaths) {
        this.dataUserAgreementPaths = dataUserAgreementPaths;
    }

    public boolean isChallenge() {
        return challenge;
    }

    public void setChallenge(boolean challenge) {
        this.challenge = challenge;
    }

    /**
     * @return the tags
     */
    public List<TagDTO> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    public List<StudyTagDTO> getStudyTags() {
        return studyTags;
    }

    public void setStudyTags(List<StudyTagDTO> studyTags) {
        this.studyTags = studyTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public StudyStorageVolumeDTO getStorageVolume() {
        return storageVolume;
    }

    public void setStorageVolume(StudyStorageVolumeDTO storageVolume) {
        this.storageVolume = storageVolume;
    }

    @Override
    public String toString() {
        return "StudyDTO [clinical=" + clinical + ", downloadableByDefault=" + downloadableByDefault + ", endDate="
                + endDate + ", experimentalGroupsOfSubjects=" + experimentalGroupsOfSubjects + ", id=" + id
                + ", name=" + name + ", profile=" + profile + ", nbExaminations="
                + nbExaminations + ", nbSubjects=" + nbSubjects + ", protocolFilePaths=" + protocolFilePaths
                + ", dataUserAgreementPaths=" + dataUserAgreementPaths + ", tags=" + tags + ", studyTags=" + studyTags
                + ", startDate=" + startDate + ", studyCards=" + studyCards + ", studyCenterList=" + studyCenterList
                + ", studyStatus=" + studyStatus + ", studyType=" + studyType + ", subjectStudyList=" + subjectStudyList
                + ", subjects=" + subjects + ", timepoints=" + timepoints + ", visibleByDefault="
                + visibleByDefault + ", withExamination=" + withExamination + ", studyUserList=" + studyUserList
                + ", challenge=" + challenge + ", description=" + description + ", license=" + license
                + ", storageVolume=" + storageVolume + "]";
    }

}
