package org.shanoir.ng.dataset.dto;

import org.shanoir.ng.dataset.model.DatasetExpressionFormat;

public class SizeByFormatDTO {

    private DatasetExpressionFormat format;
    private Long size;

    public SizeByFormatDTO(DatasetExpressionFormat format, Long size) {
        this.format = format;
        this.size = size;
    }

    public DatasetExpressionFormat getFormat() {
        return format;
    }

    public void setFormat(DatasetExpressionFormat format) {
        this.format = format;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
