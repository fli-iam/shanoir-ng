package org.shanoir.ng.dataset.dto;

import org.shanoir.ng.processing.dto.DatasetProcessingDTO;

import javax.xml.crypto.Data;
import java.util.List;

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
