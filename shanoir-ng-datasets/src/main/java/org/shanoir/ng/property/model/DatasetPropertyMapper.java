package org.shanoir.ng.property.model;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatasetPropertyMapper {

    public List<DatasetPropertyDTO> datasetPropertiesToDatasetPropertyDTOs(List<DatasetProperty> properties){
        List<DatasetPropertyDTO> dtos = new ArrayList<>();
        for(DatasetProperty property : properties){
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
