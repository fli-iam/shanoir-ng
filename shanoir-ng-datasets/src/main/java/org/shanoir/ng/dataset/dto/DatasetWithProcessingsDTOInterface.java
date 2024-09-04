package org.shanoir.ng.dataset.dto;

import org.shanoir.ng.processing.dto.DatasetProcessingDTO;

import java.util.List;

public interface DatasetWithProcessingsDTOInterface {
    List<DatasetProcessingDTO> getProcessings();

    void setProcessings(List<DatasetProcessingDTO> processings);
}
