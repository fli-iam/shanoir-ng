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

package org.shanoir.ng.dataset.modality;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.*;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMetadataMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

/**
 * Mapper for datasets.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { DatasetMetadataMapper.class, DatasetAcquisitionMapper.class })
@DecoratedWith(MrDatasetDecorator.class)
public interface MrDatasetMapper {

	
	/**
	 * Map list of @Dataset to list of @IdNameDTO.
	 * 
	 * @param datasets
	 *            list of datasets.
	 * @return list of datasets DTO.
	 */
	List<IdName> datasetsToIdNameDTOs(List<MrDataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@Named(value = "standard")
	@Mappings({ @Mapping(target = "source", ignore = true), @Mapping(target = "copies", ignore = true) })
	MrDatasetDTO datasetToDatasetDTO(MrDataset dataset);
	
	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@Named(value = "withProcessings")
	@Mapping(target = "copies", expression = "java(mapCopiesFromDataset(dataset.getCopies()))")
	@Mapping(target = "source", expression = "java(mapSourceFromDataset(dataset.getSource()))")
	MrDatasetWithDependenciesDTO datasetToDatasetAndProcessingsDTO(MrDataset dataset);
	
	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@IterableMapping(qualifiedByName = "standard")
	List<MrDatasetDTO> datasetToDatasetDTO(List<MrDataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@IterableMapping(qualifiedByName = "standard")
	PageImpl<MrDatasetDTO> datasetToDatasetDTO(Page<MrDataset> page);

	/**
	 * Map a @Dataset to a @IdNameDTO.
	 * 
	 * @param dataset
	 *            dataset to map.
	 * @return dataset DTO.
	 */
	IdName datasetToIdNameDTO(MrDataset dataset);

	default List<Long> mapCopiesFromDataset(List<Dataset> copies) {
		if (copies == null) {
			return null;
		}
		return copies.stream()
				.map(Dataset::getId)
				.collect(Collectors.toList());
	}

	default Long mapSourceFromDataset(Dataset source) {
		return source != null ? source.getId() : null;
	}
}
