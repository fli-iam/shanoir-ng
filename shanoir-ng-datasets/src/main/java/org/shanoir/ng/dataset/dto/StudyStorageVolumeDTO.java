package org.shanoir.ng.dataset.dto;

import java.util.List;

public class StudyStorageVolumeDTO {

    private Long studyId;

    private Long total = 0L;

    private List<SizeByFormatDTO> sizesByExpressionFormat;

    private Long extraDataSize;

    public StudyStorageVolumeDTO(Long studyId, List<SizeByFormatDTO> sizesByExpressionFormat, Long extraDataSize) {
        this.studyId = studyId;
        this.sizesByExpressionFormat = sizesByExpressionFormat;
        if (sizesByExpressionFormat != null){
            sizesByExpressionFormat.forEach(dto -> this.total += dto.getSize());
        }
        this.extraDataSize = extraDataSize;
        if (extraDataSize != null){
            this.total += extraDataSize;
        }

    }

    public StudyStorageVolumeDTO(Long studyId, Long total) {
        this.studyId = studyId;
        this.total = total;
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

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }
}
