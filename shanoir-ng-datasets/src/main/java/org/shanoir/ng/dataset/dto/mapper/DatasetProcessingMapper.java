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

package org.shanoir.ng.dataset.dto.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;

@Mapper(componentModel = "spring", uses = { DatasetMapper.class })
public interface DatasetProcessingMapper {

	/**
	 * Map a @DatasetMetadata to a @DatasetMetadataDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	DatasetProcessingDTO datasetProcessingToDatasetProcessingDTO(DatasetProcessing processing);

	/**
	 * Map list of @DatasetProcessing to list of @DatasetProcessingDTO.
	 * 
	 * @param dataset processings
	 *            list of dataset processings.
	 * @return list of dataset processings DTO.
	 */
	List<DatasetProcessingDTO> datasetProcessingsToDatasetProcessingDTOs(List<DatasetProcessing> datasetProcessings);
}
