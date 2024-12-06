package org.shanoir.ng.vip.planning.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.vip.monitoring.model.PipelineParameter;

import java.util.List;

/**
 * This class represents the associated criterias for an automatic execution realized after an import in shanoir.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlannedExecution extends HalEntity {

    private String name;

    private Long studyId;

    private String vipPipeline;

    private String examinationNameFilter;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "execution_pipeline_parameters")
    private List<PipelineParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVipPipeline() {
        return vipPipeline;
    }

    public void setVipPipeline(String execution) {
        this.vipPipeline = execution;
    }

    public List<PipelineParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<PipelineParameter> parameters) {
        this.parameters = parameters;
    }

    public String getExaminationNameFilter() { return examinationNameFilter; }

    public void setExaminationNameFilter(String examinationNameFilter) { this.examinationNameFilter = examinationNameFilter; }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }
}
