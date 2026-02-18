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

package org.shanoir.ng.shared.email;

import java.time.LocalDate;
import java.util.List;

public class EmailStudy extends EmailBase {
    private String description;
    private String license;
    private LocalDate startDate;
    private LocalDate endDate;
    private String studyStatus;
    private String profile;
    private String studyCardPolicy;
    private boolean clinical;
    private boolean challenge;
    private Long expectedNbOfSubjects;
    private Float averageExaminationSize;
    private Float estimatedTotalVolume;
    private Long expectedNbOfCenters;
    private Long inclusionRate;
    private String inclusionRateUnit;
    private String sponsor;
    private String principalInvestigator;
    private String scientificAdvisor;
    private List<Long> studyUsers;

    public String getDescription() {
        return description;
    }

    public void setDescription(String publicDescription) {
        this.description = publicDescription;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStudyStatus() {
        return studyStatus;
    }

    public void setStudyStatus(String studyStatus) {
        this.studyStatus = studyStatus;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getStudyCardPolicy() {
        return studyCardPolicy;
    }

    public void setStudyCardPolicy(String studyCardPolicy) {
        this.studyCardPolicy = studyCardPolicy;
    }

    public boolean isClinical() {
        return clinical;
    }

    public void setClinical(boolean clinical) {
        this.clinical = clinical;
    }

    public boolean isChallenge() {
        return challenge;
    }

    public void setChallenge(boolean challenge) {
        this.challenge = challenge;
    }

    public Long getExpectedNbOfSubjects() {
        return expectedNbOfSubjects;
    }

    public void setExpectedNbOfSubjects(Long expectedNbOfSubjects) {
        this.expectedNbOfSubjects = expectedNbOfSubjects;
    }

    public Float getAverageExaminationSize() {
        return averageExaminationSize;
    }

    public void setAverageExaminationSize(Float averageExaminationSize) {
        this.averageExaminationSize = averageExaminationSize;
    }

    public Float getEstimatedTotalVolume() {
        return estimatedTotalVolume;
    }

    public void setEstimatedTotalVolume(Float estimatedTotalVolume) {
        this.estimatedTotalVolume = estimatedTotalVolume;
    }

    public Long getExpectedNbOfCenters() {
        return expectedNbOfCenters;
    }

    public void setExpectedNbOfCenters(Long expectedNbOfCenters) {
        this.expectedNbOfCenters = expectedNbOfCenters;
    }

    public Long getInclusionRate() {
        return inclusionRate;
    }

    public void setInclusionRate(Long inclusionRate) {
        this.inclusionRate = inclusionRate;
    }

    public String getInclusionRateUnit() {
        return inclusionRateUnit;
    }

    public void setInclusionRateUnit(String inclusionRateUnit) {
        this.inclusionRateUnit = inclusionRateUnit;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public String getScientificAdvisor() {
        return scientificAdvisor;
    }

    public void setScientificAdvisor(String scientificAdvisor) {
        this.scientificAdvisor = scientificAdvisor;
    }

    public List<Long> getStudyUsers() {
        return studyUsers;
    }

    public void setStudyUsers(List<Long> studyUsers) {
        this.studyUsers = studyUsers;
    }
}
