package org.shanoir.ng.vip.executionTemplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;

import java.util.List;

/**
 * This class represents the associated criterias for an automatic execution realized after an import in shanoir.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionTemplateDTO extends HalEntity {

    private String name;
    private long studyId;
    private String vipPipeline;
    private List<ExecutionTemplateParameter> parameters;
    private int priority;

    public int getPriority() {return priority;}

    public void setPriority(int priority) {this.priority = priority;}

    public long getStudyId() { return studyId; }

    public void setStudyId(long studyId) { this.studyId = studyId; }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getVipPipeline() {return vipPipeline;}

    public void setVipPipeline(String vipPipeline) {this.vipPipeline = vipPipeline;}

    public List<ExecutionTemplateParameter> getParameters() {return parameters;}

    public void setParameters(List<ExecutionTemplateParameter> parameters) {this.parameters = parameters;}
}
