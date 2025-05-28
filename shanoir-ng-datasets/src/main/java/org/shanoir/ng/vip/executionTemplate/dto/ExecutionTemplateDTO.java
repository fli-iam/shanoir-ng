package org.shanoir.ng.vip.executionTemplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.shared.hateoas.HalEntity;

import java.util.List;

/**
 * This class represents the associated criterias for an automatic execution realized after an import in shanoir.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionTemplateDTO extends HalEntity {

    private String name;
    private Long studyId;
    private String pipelineName;
    private List<ExecutionTemplateParameterDTO> parameters;
    private List<ExecutionTemplateFilterDTO> filters;
    private int priority;
    private String filterCombination;

    public String getFilterCombination() {return filterCombination;}

    public void setFilterCombination(String filterCombination) {this.filterCombination = filterCombination;}

    public int getPriority() {return priority;}

    public void setPriority(int priority) {this.priority = priority;}

    public Long getStudyId() { return studyId; }

    public void setStudyId(Long studyId) { this.studyId = studyId; }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getPipelineName() {return pipelineName;}

    public void setPipelineName(String pipelineName) {this.pipelineName = pipelineName;}

    public List<ExecutionTemplateFilterDTO> getFilters() {return filters;}

    public void setFilters(List<ExecutionTemplateFilterDTO> filtersDTO) {this.filters = filtersDTO;}

    public List<ExecutionTemplateParameterDTO> getParameters() {return parameters;}

    public void setParameters(List<ExecutionTemplateParameterDTO> parametersDTO) {this.parameters = parametersDTO;}
}