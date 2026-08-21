/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
