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

import org.shanoir.ng.study.model.StudyExtraDetails;
import org.shanoir.ng.study.model.InclusionRateUnit;

/**
 * DTO for studyExtraDetails.
 *
 * @author Adam Fragkiadakis
 *
 */

public class StudyExtraDetailsDTO {

    /** Expected number of subjects. */
    private Long expectedNbOfSubjects;

    /** Average examination size in MB. */
    private Float averageExaminationSize;

    /** Estimated total volume in MB. */
    private Float estimatedTotalVolume;

    /** Expected number of centers. */
    private Long expectedNbOfCenters;

    /** Inclusion rate. */
    private Long inclusionRate;

    /** Inclusion rate unit. */
    private InclusionRateUnit inclusionRateUnit;

    /** Sponsor ID. */
    private String sponsor;

    /** Principal investigator ID. */
    private String principalInvestigator;

    /** Scientific advisor ID. */
    private String scientificAdvisor;

    public StudyExtraDetailsDTO(StudyExtraDetails extraDetails) {
        if (extraDetails == null) {
            return;
        }
        this.expectedNbOfSubjects = extraDetails.getExpectedNbOfSubjects();
        this.averageExaminationSize = extraDetails.getAverageExaminationSize();
        this.estimatedTotalVolume = extraDetails.getEstimatedTotalVolume();
        this.expectedNbOfCenters = extraDetails.getExpectedNbOfCenters();
        this.inclusionRate = extraDetails.getInclusionRate();
        this.inclusionRateUnit = extraDetails.getInclusionRateUnit();
        this.sponsor = extraDetails.getSponsor();
        this.principalInvestigator = extraDetails.getPrincipalInvestigator();
        this.scientificAdvisor = extraDetails.getScientificAdvisor();
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

    public InclusionRateUnit getInclusionRateUnit() {
        return inclusionRateUnit;
    }

    public void setInclusionRateUnit(InclusionRateUnit inclusionRateUnit) {
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
}
