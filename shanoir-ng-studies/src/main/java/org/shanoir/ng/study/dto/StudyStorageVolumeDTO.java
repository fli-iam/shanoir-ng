package org.shanoir.ng.study.dto;

import java.util.ArrayList;
import java.util.List;

public class StudyStorageVolumeDTO {
    private Long total = 0L;

    private List<VolumeByFormatDTO> volumeByFormat = new ArrayList<>();

    private Long extraDataSize = 0L;

    public StudyStorageVolumeDTO() {
    }

    public List<VolumeByFormatDTO> getVolumeByFormat() {
        return volumeByFormat;
    }

    public void setVolumeByFormat(List<VolumeByFormatDTO> volumeByFormat) {
        this.volumeByFormat = volumeByFormat;
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
