package org.shanoir.ng.dataset.dto;

import java.util.List;

import org.shanoir.ng.processing.dto.DatasetProcessingDTO;

public class DatasetWithProcessingsDTO extends DatasetDTO implements DatasetWithProcessingsDTOInterface {

    private List<DatasetProcessingDTO> processings;

    @Override
    public List<DatasetProcessingDTO> getProcessings() {
        return processings;
    }

    @Override
    public void setProcessings(List<DatasetProcessingDTO> processings) {
        this.processings = processings;
    }

}
