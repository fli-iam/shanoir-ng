package org.shanoir.ng.dataset.modality;

import org.shanoir.ng.dataset.dto.DatasetWithProcessingsDTOInterface;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;

import java.util.List;

public class MrDatasetWithProcessingsDTO extends MrDatasetDTO implements DatasetWithProcessingsDTOInterface {

    private List<DatasetProcessingDTO> processings;

    @Override
    public List<DatasetProcessingDTO> getProcessings() {
        return processings;
    }

    @Override
    public void setProcessings(List<DatasetProcessingDTO> datasetProcessings) {
        this.processings = datasetProcessings;
    }


}
