package org.shanoir.ng.shared.dataset;

import java.util.List;

public class RelatedDataset {

    private Long studyId;

    private List<Long> datasetIds;

    private Long userId;

    public RelatedDataset() {
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
