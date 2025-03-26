package org.shanoir.ng.processing.dto.mapper;

import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public abstract class DatasetProcessingDecorator implements DatasetProcessingMapper {

    @Autowired
    private DatasetProcessingMapper delegate;

    @Override
    public DatasetProcessingDTO datasetProcessingToDatasetProcessingDTO(DatasetProcessing processing) {

        if ( processing == null ) {
            return null;
        }

        DatasetProcessingDTO dto = delegate.datasetProcessingToDatasetProcessingDTO(processing);
        if(processing.getParent() != null) {
            dto.setParentId(processing.getParent().getId());
        }
        return dto;
    }

    @Override
    public List<DatasetProcessingDTO> datasetProcessingsToDatasetProcessingDTOs(List<DatasetProcessing> processings) {
        // When loading multiple processing, remove input datasets to avoid loading too much data #2121
        return processings.stream().map(this::datasetProcessingToDatasetProcessingDTO).peek(dto -> dto.setInputDatasets(null)).collect(Collectors.toList());
    }
}
