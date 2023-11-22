package org.shanoir.ng.study.dto;

import java.util.List;

public class RelatedDatasetDTO {

    private Long studyId;

    private List<Long> datasetIds;

    private Long userId;

    public RelatedDatasetDTO() {
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public List<Long> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<Long> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
