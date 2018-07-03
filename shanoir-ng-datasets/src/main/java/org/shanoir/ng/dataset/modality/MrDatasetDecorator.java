package org.shanoir.ng.dataset.modality;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Decorator for dataset acquisitions mapper.
 * 
 * @author msimon
 *
 */
public abstract class MrDatasetDecorator implements MrDatasetMapper {

	@Autowired
	private MrDatasetMapper delegate;

	@Override
	public List<IdNameDTO> datasetsToIdNameDTOs(final List<MrDataset> datasets) {
		final List<IdNameDTO> datasetDTOs = new ArrayList<>();
		for (MrDataset dataset : datasets) {
			datasetDTOs.add(datasetToIdNameDTO(dataset));
		}
		return datasetDTOs;
	}

	@Override
	public IdNameDTO datasetToIdNameDTO(final MrDataset dataset) {
		return delegate.datasetToIdNameDTO(dataset);
	}

}
