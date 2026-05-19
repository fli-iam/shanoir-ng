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

package org.shanoir.ng.property.model;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatasetPropertyMapper {

    public List<DatasetPropertyDTO> datasetPropertiesToDatasetPropertyDTOs(List<DatasetProperty> properties) {
        List<DatasetPropertyDTO> dtos = new ArrayList<>();
        for (DatasetProperty property : properties) {
            dtos.add(this.datasetPropertyToDatasetPropertyDTO(property));
        }
        return dtos;
    }

    private DatasetPropertyDTO datasetPropertyToDatasetPropertyDTO(DatasetProperty property) {
        DatasetPropertyDTO dto = new DatasetPropertyDTO();
        dto.setDatasetId(property.getDataset().getId());
        dto.setProcessingId(property.getProcessing().getId());
        dto.setName(property.getName());
        dto.setValue(property.getName());
        return dto;
    }

}
