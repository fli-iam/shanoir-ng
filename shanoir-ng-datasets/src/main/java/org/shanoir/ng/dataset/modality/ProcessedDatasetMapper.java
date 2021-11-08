package org.shanoir.ng.dataset.modality;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.shanoir.ng.dataset.dto.mapper.DatasetMetadataMapper;
import org.shanoir.ng.shared.core.model.IdName;

@Mapper(componentModel = "spring", uses = { DatasetMetadataMapper.class })
@DecoratedWith(ProcessedDatasetDecorator.class)
public interface ProcessedDatasetMapper {

	/**
	 * Map a @Dataset to a @IdNameDTO.
	 * 
	 * @param dataset
	 *            dataset to map.
	 * @return dataset DTO.
	 */
	IdName datasetToIdNameDTO(ProcessedDataset dataset);

	/**
	 * Map list of @Dataset to list of @IdNameDTO.
	 * 
	 * @param datasets
	 *            list of datasets.
	 * @return list of datasets DTO.
	 */
	List<IdName> datasetsToIdNameDTOs(List<ProcessedDataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@Named(value = "standard")
	ProcessedDatasetDTO datasetToDatasetDTO(ProcessedDataset dataset);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	@IterableMapping(qualifiedByName = "standard")
	List<ProcessedDatasetDTO> datasetToDatasetDTO(List<ProcessedDataset> datasets);

}
