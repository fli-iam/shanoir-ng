package org.shanoir.ng.property.model;

public class DatasetPropertyDTO {

    private Long datasetId;

    private Long processingId;

    private String name;

    private String value;

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Long getProcessingId() {
        return processingId;
    }

    public void setProcessingId(Long processingId) {
        this.processingId = processingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
