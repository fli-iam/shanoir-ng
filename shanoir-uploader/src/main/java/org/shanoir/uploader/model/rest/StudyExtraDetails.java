package org.shanoir.uploader.model.rest;

public class StudyExtraDetails {

    private Long expectedNbOfSubjects;

    private Float averageExaminationSize;

    private Float estimatedTotalVolume;

    private Long expectedNbOfCenters;

    private Long inclusionRate;

    private Integer inclusionRateUnit;

    private String sponsor;

    private String principalInvestigator;

    private String scientificAdvisor;

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

    public Integer getInclusionRateUnit() {
        return inclusionRateUnit;
    }

    public void setInclusionRateUnit(Integer inclusionRateUnit) {
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
