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

package org.shanoir.ng.datasetacquisition;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.dataset.DatasetMapper;

/**
 * Mapper for dataset acquisitions.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { DatasetMapper.class })
@DecoratedWith(DatasetAcquisitionDecorator.class)
public interface DatasetAcquisitionMapper {

	/**
	 * Map list of @DatasetAcquisition to list
	 * of @ExaminationDatasetAcquisitionDTO.
	 * 
	 * @param datasetAcquisitions
	 *            list of dataset acquisitions.
	 * @return list of dataset acquisitions DTO.
	 */
	List<ExaminationDatasetAcquisitionDTO> datasetAcquisitionsToExaminationDatasetAcquisitionDTOs(
			List<DatasetAcquisition> datasetAcquisitions);

	/**
	 * Map a @DatasetAcquisition to a @ExaminationDatasetAcquisitionDTO.
	 * 
	 * @param datasetAcquisition
	 *            dataset acquisition to map.
	 * @return dataset acquisition DTO.
	 */
	@Mappings({ @Mapping(target = "name", ignore = true) })
	ExaminationDatasetAcquisitionDTO datasetAcquisitionToExaminationDatasetAcquisitionDTO(
			DatasetAcquisition datasetAcquisition);

}
