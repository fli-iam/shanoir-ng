package org.shanoir.ng.vip.planning.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.vip.monitoring.model.PipelineParameter;

import java.util.List;

/**
 * This class represents the associated criterias for an automatic execution realized after an import in shanoir.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlannedExecution {

    @Id
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "study_id")
    private Study study;

    private String execution;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "execution_pipeline_parameters")
    private List<PipelineParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public String getExecution() {
        return execution;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

    public List<PipelineParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<PipelineParameter> parameters) {
        this.parameters = parameters;
    }
}
