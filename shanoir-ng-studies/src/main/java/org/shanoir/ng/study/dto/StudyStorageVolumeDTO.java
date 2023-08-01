package org.shanoir.ng.study.dto;

import java.util.List;

public class StudyStorageVolumeDTO {

    private Long studyId;

    private Long total = 0L;

    private List<SizeByFormatDTO> sizesByExpressionFormat;

    private Long extraDataSize;

    public StudyStorageVolumeDTO(List<SizeByFormatDTO> sizesByExpressionFormat, Long extraDataSize) {
        this.sizesByExpressionFormat = sizesByExpressionFormat;
        if (sizesByExpressionFormat != null){
            sizesByExpressionFormat.forEach(dto -> this.total += dto.getSize());
        }
        this.extraDataSize = extraDataSize;
        if (extraDataSize != null){
            this.total += extraDataSize;
        }


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
