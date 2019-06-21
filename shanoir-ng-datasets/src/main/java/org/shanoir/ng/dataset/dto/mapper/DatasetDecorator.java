/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/dataset/dto/mapper/DatasetDecorator.java
package org.shanoir.ng.dataset.dto.mapper;
=======
package org.shanoir.ng.dataset;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/dataset/DatasetDecorator.java

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
