package org.shanoir.ng.dataset.dto.mapper;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;

/**
 * Decorator for dataset acquisitions mapper.
 * 
 * @author msimon
 * @author jlouis
 *
 */
public abstract class DatasetDecorator implements DatasetMapper {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetDecorator.class);

	@Autowired
	private DatasetMapper defaultMapper;

	@Autowired
	private MrDatasetMapper mrMapper;

	@Override
	public List<IdName> datasetsToIdNameDTOs(final List<Dataset> datasets) {
		final List<IdName> datasetDTOs = new ArrayList<>();
		for (Dataset dataset : datasets) {
			datasetDTOs.add(datasetToIdNameDTO(dataset));
		}
		return datasetDTOs;
	}

	@Override
	public PageImpl<DatasetDTO> datasetToDatasetDTO(Page<Dataset> page) {
		Page<DatasetDTO> mappedPage = page.map(new Converter<Dataset, DatasetDTO>() {
			@Override
			public DatasetDTO convert(Dataset entity) {
				if (entity instanceof MrDataset) {
					return mrMapper.datasetToDatasetDTO((MrDataset)entity);
				}
				// TODO : Complete
				else {
					return defaultMapper.datasetToDatasetDTO(entity);
				}
			}
		});
		return new PageImpl<DatasetDTO>(mappedPage);
	}

	@Override
	public IdName datasetToIdNameDTO(final Dataset dataset) {
		return defaultMapper.datasetToIdNameDTO(dataset);
	}

}
