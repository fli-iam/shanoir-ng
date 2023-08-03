package org.shanoir.ng.study.dto;

import java.util.ArrayList;
import java.util.List;

public class StudyStorageVolumeDTO {
    private Long total = 0L;

    private List<SizeByFormatDTO> sizesByExpressionFormat = new ArrayList<>();

    private Long extraDataSize = 0L;

    public StudyStorageVolumeDTO() {
    }

    public List<SizeByFormatDTO> getSizesByExpressionFormat() {
        return sizesByExpressionFormat;
    }

    public void setSizesByExpressionFormat(List<SizeByFormatDTO> sizesByExpressionFormat) {
        this.sizesByExpressionFormat = sizesByExpressionFormat;
    }

    public Long getExtraDataSize() {
        return extraDataSize;
    }

    public void setExtraDataSize(Long extraDataSize) {
        this.extraDataSize = extraDataSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

}
