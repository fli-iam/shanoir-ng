package org.shanoir.ng.vip.shared.dto;

import org.shanoir.ng.processing.dto.GroupByEnum;

import java.util.List;

public class DatasetParameterDTO {

    private String name;
    private GroupByEnum groupBy;
    private String exportFormat;
    private List<Long> datasetIds;
    private Long converterId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public Long getConverterId() {
        return converterId;
    }

    public void setConverterId(Long converterId) {
        this.converterId = converterId;
    }
}
