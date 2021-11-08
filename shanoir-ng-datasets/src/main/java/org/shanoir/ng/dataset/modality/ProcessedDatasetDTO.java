package org.shanoir.ng.dataset.modality;

import java.util.List;

import org.shanoir.ng.dataset.dto.DatasetAndProcessingsDTOInterface;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;

public class ProcessedDatasetDTO extends DatasetDTO implements DatasetAndProcessingsDTOInterface {

	private String processedType;

	private List<DatasetProcessingDTO> processings;
	
	private DatasetAcquisitionDTO datasetAcquisition;

	@Override
	public DatasetAcquisitionDTO getDatasetAcquisition() {
		return this.datasetAcquisition;
	}

	@Override
	public void setDatasetAcquisition(DatasetAcquisitionDTO datasetAcquisition) {
		this.datasetAcquisition = datasetAcquisition;
	}

	@Override
	public List<DatasetProcessingDTO> getProcessings() {
		return processings;
	}

	@Override
	public void setProcessings(List<DatasetProcessingDTO> datasetProcessings) {
		this.processings = datasetProcessings;
	}

	public String getProcessedType() {
		return processedType;
	}

	public void setProcessedType(String processedType) {
		this.processedType = processedType;
	}
}
