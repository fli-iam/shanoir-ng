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

import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Study Extra Details.
 *
 * @author Adam Fragkiadakis
 *
 */
@Entity
@Table(name = "study_extra_details")
public class StudyExtraDetails extends HalEntity {

    /**
     * UID
     */
    private static final long serialVersionUID = 1L;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "study_id")
    private Study study;

    /** Expected number of subjects. */
    @NotNull
    @Column(name = "expected_nb_of_subjects")
    private Long expectedNbOfSubjects;

    /** Average examination size in MB. */
    @Column(name = "average_examination_size")
    private Float averageExaminationSize;

    /** Estimated total volume in MB. */
    @Column(name = "estimated_total_volume")
    private Float estimatedTotalVolume;

    /** Expected number of centers. */
    @NotNull
    @Column(name = "expected_nb_of_centers")
    private Long expectedNbOfCenters;

    /** Inclusion rate. */
    @Column(name = "inclusion_rate")
    private Long inclusionRate;

    /** Inclusion rate unit. */
    @Column(name = "inclusion_rate_unit")
    private Integer inclusionRateUnit;

    /** Sponsor ID. */
    @NotNull
    @Column(name = "sponsor")
    private String sponsor;

    /** Principal investigator ID. */
    @NotNull
    @Column(name = "principal_investigator")
    private String principalInvestigator;

    /** Scientific advisor ID. */
    @Column(name = "scientific_advisor")
    private String scientificAdvisor;

    /**
     * @return the expectedNbOfSubjects
     */
    public Long getExpectedNbOfSubjects() {
        return expectedNbOfSubjects;
    }

    /**
     * @param expectedNbOfSubjects the expectedNbOfSubjects to set
     */
    public void setExpectedNbOfSubjects(Long expectedNbOfSubjects) {
        this.expectedNbOfSubjects = expectedNbOfSubjects;
    }

    /**
     * @return the averageExaminationSize
     */
    public Float getAverageExaminationSize() {
        return averageExaminationSize;
    }

    /**
     * @param averageExaminationSize the averageExaminationSize to set
     */
    public void setAverageExaminationSize(Float averageExaminationSize) {
        this.averageExaminationSize = averageExaminationSize;
    }

    /**
     * @return the estimatedTotalVolume
     */
    public Float getEstimatedTotalVolume() {
        return estimatedTotalVolume;
    }

    /**
     * @param estimatedTotalVolume the estimatedTotalVolume to set
     */
    public void setEstimatedTotalVolume(Float estimatedTotalVolume) {
        this.estimatedTotalVolume = estimatedTotalVolume;
    }

    /**
     * @return the expectedNbOfCenters
     */
    public Long getExpectedNbOfCenters() {
        return expectedNbOfCenters;
    }

    /**
     * @param expectedNbOfCenters the expectedNbOfCenters to set
     */
    public void setExpectedNbOfCenters(Long expectedNbOfCenters) {
        this.expectedNbOfCenters = expectedNbOfCenters;
    }

    /**
     * @return the inclusionRate
     */
    public Long getInclusionRate() {
        return inclusionRate;
    }

    /**
     * @param inclusionRate the inclusionRate to set
     */
    public void setInclusionRate(Long inclusionRate) {
        this.inclusionRate = inclusionRate;
    }

    /**
     * @return the inclusionRateUnit
     */
    public InclusionRateUnit getInclusionRateUnit() {
        return InclusionRateUnit.getUnit(inclusionRateUnit);
    }

    /**
     * @param inclusionRateUnit the inclusionRateUnit to set
     */
    public void setInclusionRateUnit(InclusionRateUnit inclusionRateUnit) {
        if (inclusionRateUnit != null) {
            this.inclusionRateUnit = inclusionRateUnit.getId();
        }
    }

    /**
     * @return the sponsor
     */
    public String getSponsor() {
        return sponsor;
    }

    /**
     * @param sponsor the sponsor to set
     */
    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    /**
     * @return the principalInvestigator
     */
    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    /**
     * @param principalInvestigator the principalInvestigator to set
     */
    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    /**
     * @return the scientificAdvisor
     */
    public String getScientificAdvisor() {
        return scientificAdvisor;
    }

    /**
     * @param scientificAdvisor the scientificAdvisor to set
     */
    public void setScientificAdvisor(String scientificAdvisor) {
        this.scientificAdvisor = scientificAdvisor;
    }
}
