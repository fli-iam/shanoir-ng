package org.shanoir.ng.processing.dto;

import java.util.List;

public class ParameterResourcesDTO {

    private String parameter;
    private List<String> resourceIds;
    private GroupByEnum groupBy;
    List<Long> datasetIds;

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public GroupByEnum getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(GroupByEnum groupBy) {
        this.groupBy = groupBy;
    }

    public List<Long> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<Long> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }
}
