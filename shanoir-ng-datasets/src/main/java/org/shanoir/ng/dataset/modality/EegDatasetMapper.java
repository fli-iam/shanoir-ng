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

import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.shanoir.ng.dataset.dto.mapper.DatasetMetadataMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

/**
 * Mapper for datasets.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { DatasetMetadataMapper.class })
@DecoratedWith(EegDatasetDecorator.class)
public interface EegDatasetMapper {

	
	/**
	 * Map list of @Dataset to list of @IdNameDTO.
	 * 
	 * @param datasets
	 *            list of datasets.
	 * @return list of datasets DTO.
	 */
	List<IdName> datasetsToIdNameDTOs(List<EegDataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@Named(value = "standard")
	EegDatasetDTO datasetToDatasetDTO(EegDataset dataset);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@Named(value = "withProcessings")
	EegDatasetWithDependenciesDTO datasetToDatasetAndProcessingsDTO(EegDataset dataset);
	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@IterableMapping(qualifiedByName = "standard")
	List<EegDatasetDTO> datasetToDatasetDTO(List<EegDataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@IterableMapping(qualifiedByName = "standard")
	PageImpl<EegDatasetDTO> datasetToDatasetDTO(Page<EegDataset> page);

	/**
	 * Map a @Dataset to a @IdNameDTO.
	 * 
	 * @param dataset
	 *            dataset to map.
	 * @return dataset DTO.
	 */
	IdName datasetToIdNameDTO(EegDataset dataset);

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
	default List<Long> mapCopiesFromDatasetAcquisition(List<DatasetAcquisition> copies) {
		if (copies == null) {
			return null;
		}
		return copies.stream()
				.map(DatasetAcquisition::getId)
				.collect(Collectors.toList());
	}

	default Long mapSourceFromDatasetAcquisition(DatasetAcquisition source) {
		return source != null ? source.getId() : null;
	}

	default List<DatasetAcquisition> mapCopiesDatasetAcquisitionFromLong(List<Long> copies) {
		return null;
	}

	default DatasetAcquisition mapSourceDatasetAcquisitionFromLong(Long source) {
		return null;
	}

	default List<Long> mapCopiesFromExamination(List<Examination> copies) {
		if (copies == null) {
			return null;
		}
		return copies.stream()
				.map(Examination::getId)
				.collect(Collectors.toList());
	}

	default Long mapSourceFromExamination(Examination source) {
		return source != null ? source.getId() : null;
	}
}
