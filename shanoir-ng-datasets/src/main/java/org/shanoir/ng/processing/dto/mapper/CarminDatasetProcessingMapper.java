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

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetUtils;
import org.shanoir.ng.processing.carmin.model.ExecutionMonitoring;
import org.shanoir.ng.processing.dto.ExecutionMonitoringDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = { DatasetProcessingMapper.class, DatasetMapper.class })
public interface CarminDatasetProcessingMapper {

	/**
	 * Map a @CarminDatasetProcessing to a @CarminDatasetProcessingDTO.
	 * 
	 * @param processing
	 *            dataset.
	 * @return dataset DTO.
	 */
	@Mapping(target = "parametersResources", ignore = true)
	ExecutionMonitoringDTO carminDatasetProcessingToCarminDatasetProcessingDTO(ExecutionMonitoring processing);

	/**
	 * Map list of @CarminDatasetProcessing to list of @CarminDatasetProcessingDTO.
	 * 
	 * @param datasetProcessings processings
	 *            list of dataset processings.
	 * @return list of dataset processings DTO.
	 */
	List<ExecutionMonitoringDTO> carminDatasetProcessingsToCarminDatasetProcessingDTOs(List<ExecutionMonitoring> datasetProcessings);

	/**
	 * Map @CarminDatasetProcessingDTO to @CarminDatasetProcessing
	 *
	 * @param dto
	 * @return
	 */
	ExecutionMonitoring carminDatasetProcessingDTOToCarminDatasetProcessing(ExecutionMonitoringDTO dto);

	@ObjectFactory
	default Dataset createDataset(DatasetDTO dto) {
		return DatasetUtils.buildDatasetFromType(dto.getType());
	}


}
