package org.shanoir.ng.processing.carmin.dto.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.shanoir.ng.processing.carmin.dto.CarminDatasetProcessingDTO;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;

@Mapper(componentModel = "spring")
public interface CarminDatasetProcessingMapper {
    
    CarminDatasetProcessingDTO carminDatasetProcessingToCarminDatasetProcessingDTO(CarminDatasetProcessing carminProcessing);

    List<CarminDatasetProcessingDTO> carminDatasetProcessingsToCarminDatasetProcessingDTOs(List<CarminDatasetProcessing> carminProcessings);
}
