package org.shanoir.ng.dataset;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

/**
 * Mapper for datasets.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { DatasetMetadataMapper.class })
@DecoratedWith(DatasetDecorator.class)
public interface DatasetMapper {

	/**
	 * Map list of @Dataset to list of @IdNameDTO.
	 * 
	 * @param datasets
	 *            list of datasets.
	 * @return list of datasets DTO.
	 */
	List<IdNameDTO> datasetsToIdNameDTOs(List<Dataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	DatasetDTO datasetToDatasetDTO(Dataset dataset);
	
	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	List<DatasetDTO> datasetToDatasetDTO(List<Dataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	public PageImpl<DatasetDTO> datasetToDatasetDTO(Page<Dataset> page);

	/**
	 * Map a @Dataset to a @IdNameDTO.
	 * 
	 * @param dataset
	 *            dataset to map.
	 * @return dataset DTO.
	 */
	IdNameDTO datasetToIdNameDTO(Dataset dataset);

}
