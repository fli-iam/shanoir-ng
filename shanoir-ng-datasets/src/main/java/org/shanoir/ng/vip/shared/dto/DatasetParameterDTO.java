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

package org.shanoir.ng.vip.shared.dto;

import org.shanoir.ng.processing.dto.GroupByEnum;

import java.util.List;

public class DatasetParameterDTO {

    private String name;
    private GroupByEnum groupBy;
    private String exportFormat;
    private List<Long> datasetIds;
    private Long converterId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupByEnum getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(GroupByEnum groupBy) {
        this.groupBy = groupBy;
    }

    public List<Long> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<Long> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public Long getConverterId() {
        return converterId;
    }

    public void setConverterId(Long converterId) {
        this.converterId = converterId;
    }
}
