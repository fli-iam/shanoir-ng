package org.shanoir.ng.dataset.dto;

import java.util.List;

public class StudyStorageVolumeDTO {

    private Long total = 0L;

    private List<VolumeByFormatDTO> volumeByFormat;

    private Long extraDataSize;

    public StudyStorageVolumeDTO(List<VolumeByFormatDTO> volumeByFormat, Long extraDataSize) {
        this.volumeByFormat = volumeByFormat;
        if (volumeByFormat != null) {
            volumeByFormat.forEach(dto -> this.total += dto.getSize());
        }
        this.extraDataSize = extraDataSize;
        if (extraDataSize != null) {
            this.total += extraDataSize;
        }

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
