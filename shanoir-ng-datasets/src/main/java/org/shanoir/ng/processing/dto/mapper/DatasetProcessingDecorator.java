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

package org.shanoir.ng.processing.dto.mapper;

import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public abstract class DatasetProcessingDecorator implements DatasetProcessingMapper {

    @Override
    public DatasetProcessingDTO datasetProcessingToDatasetProcessingDTO(DatasetProcessing processing) {

        if (processing == null) {
            return null;
        }

        DatasetProcessingDTO dto = datasetProcessingToDatasetProcessingDTO(processing);
        if (processing.getParent() != null) {
            dto.setParentId(processing.getParent().getId());
        }
        return dto;
    }

    @Override
    public List<DatasetProcessingDTO> datasetProcessingListToDatasetProcessingDTOList(List<DatasetProcessing> processings) {
        // When loading multiple processing, remove input datasets to avoid loading too much data #2121
        return processings.stream().map(this::datasetProcessingToDatasetProcessingDTO).peek(dto -> dto.setInputDatasets(null)).collect(Collectors.toList());
    }
}
