package org.shanoir.ng.dataset.modality;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessedDatasetDecorator implements ProcessedDatasetMapper {

	@Autowired
	private ProcessedDatasetMapper delegate;


	public ProcessedDatasetDecorator() {
	}

	@Override
	public IdName datasetToIdNameDTO(ProcessedDataset dataset) {
		return delegate.datasetToIdNameDTO(dataset);
	}

	@Override
	public List<IdName> datasetsToIdNameDTOs(List<ProcessedDataset> datasets) {
		final List<IdName> datasetDTOs = new ArrayList<>();
		for (ProcessedDataset dataset : datasets) {
			datasetDTOs.add(datasetToIdNameDTO(dataset));
		}
		return datasetDTOs;
	}

	@Override
	public ProcessedDatasetDTO datasetToDatasetDTO(ProcessedDataset dataset) {
		return delegate.datasetToDatasetDTO(dataset);
	}

	@Override
	public List<ProcessedDatasetDTO> datasetToDatasetDTO(List<ProcessedDataset> datasets) {
		final List<ProcessedDatasetDTO> datasetDTOs = new ArrayList<>();
		for (ProcessedDataset dataset : datasets) {
			datasetDTOs.add(datasetToDatasetDTO(dataset));
		}
		return datasetDTOs;
	}
}
