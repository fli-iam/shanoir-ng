package org.shanoir.ng.vip.executionTemplate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.model.Study;

import java.util.List;

/**
 * This class represents the associated criterias for an automatic execution realized after an import in shanoir.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionTemplate extends HalEntity {

    private String name;
    private String pipelineName;
    private String filterCombination;
    private int priority;

    @OneToMany(mappedBy = "executionTemplate", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExecutionTemplateFilter> filters;

    @OneToMany(mappedBy = "executionTemplate", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExecutionTemplateParameter> parameters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    public List<ExecutionTemplateParameter> getParameters() {return parameters;}

    public void setParameters(List<ExecutionTemplateParameter> parameters) {this.parameters = parameters;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getPipelineName() {return pipelineName;}

    public void setPipelineName(String execution) {this.pipelineName = execution;}

    public List<ExecutionTemplateFilter> getFilters() {return filters;}

    public void setFilters(List<ExecutionTemplateFilter> filters) {this.filters = filters;}

    public String getFilterCombination() { return filterCombination; }

    public void setFilterCombination(String filterCombination) { this.filterCombination = filterCombination; }

    public Study getStudy() {return study;}

    public void setStudy(Study study) {this.study = study;}

    public int getPriority() {return priority;}

    public void setPriority(int priority) {this.priority = priority;}
}
