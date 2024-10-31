package org.shanoir.ng.vip.planning.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.vip.monitoring.model.PipelineParameter;

import java.util.List;

/**
 * This class represents the associated criterias for an automatic execution realized after an import in shanoir.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlannedExecutionDTO extends HalEntity {

    private String name;

    private long studyId;

    private String vipPipeline;

    private String examinationNameFilter;

    private List<PipelineParameter> parameters;

    public long getStudyId() { return studyId; }

    public void setStudyId(long studyId) { this.studyId = studyId; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVipPipeline() {
        return vipPipeline;
    }

    public void setVipPipeline(String vipPipeline) {
        this.vipPipeline = vipPipeline;
    }

    public String getExaminationNameFilter() {
        return examinationNameFilter;
    }

    public void setExaminationNameFilter(String examinationNameFilter) {
        this.examinationNameFilter = examinationNameFilter;
    }

    public List<PipelineParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<PipelineParameter> parameters) {
        this.parameters = parameters;
    }
}
