package org.shanoir.ng.dataset;

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
public abstract class DatasetDecorator implements DatasetMapper {

	@Autowired
	private DatasetMapper delegate;

	@Override
	public List<IdNameDTO> datasetsToIdNameDTOs(final List<Dataset> datasets) {
		final List<IdNameDTO> datasetDTOs = new ArrayList<>();
		for (Dataset dataset : datasets) {
			datasetDTOs.add(datasetToIdNameDTO(dataset));
		}
		return datasetDTOs;
	}

	@Override
	public IdNameDTO datasetToIdNameDTO(final Dataset dataset) {
		return delegate.datasetToIdNameDTO(dataset);
	}

}
