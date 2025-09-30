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

package org.shanoir.ng.processing.dto.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.dataset.dto.DatasetWithProcessingsDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;

@Mapper(componentModel = "spring", uses = { DatasetMapper.class })
@DecoratedWith(DatasetProcessingDecorator.class)
public interface DatasetProcessingMapper {

	/**
	 * Map a @DatasetMetadata to a @DatasetMetadataDTO.
	 *
	 * @param processing
	 *            dataset.
	 * @return dataset DTO.
	 */
	DatasetProcessingDTO datasetProcessingToDatasetProcessingDTO(DatasetProcessing processing);

	/**
	 * Map list of @DatasetProcessing to list of @DatasetProcessingDTO.
	 *
	 * @param datasetProcessings processings
	 *            list of dataset processings.
	 * @return list of dataset processings DTO.
	 */
	List<DatasetProcessingDTO> datasetProcessingsToDatasetProcessingDTOs(List<DatasetProcessing> datasetProcessings);

	@Mappings({ @Mapping(target = "source", ignore = true), @Mapping(target = "copies", ignore = true) })
	DatasetWithProcessingsDTO datasetToDatasetWithProcessingsDTO(Dataset dataset);
}
