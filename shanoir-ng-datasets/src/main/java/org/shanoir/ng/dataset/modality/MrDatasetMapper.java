package org.shanoir.ng.dataset.modality;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.shanoir.ng.dataset.dto.mapper.DatasetMetadataMapper;
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
@DecoratedWith(MrDatasetDecorator.class)
public interface MrDatasetMapper {

	
	/**
	 * Map list of @Dataset to list of @IdNameDTO.
	 * 
	 * @param datasets
	 *            list of datasets.
	 * @return list of datasets DTO.
	 */
	List<IdNameDTO> datasetsToIdNameDTOs(List<MrDataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	MrDatasetDTO datasetToDatasetDTO(MrDataset dataset);
	
	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	List<MrDatasetDTO> datasetToDatasetDTO(List<MrDataset> datasets);

	/**
	 * Map a @Dataset to a @DatasetDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	PageImpl<MrDatasetDTO> datasetToDatasetDTO(Page<MrDataset> page);

	/**
	 * Map a @Dataset to a @IdNameDTO.
	 * 
	 * @param dataset
	 *            dataset to map.
	 * @return dataset DTO.
	 */
	IdNameDTO datasetToIdNameDTO(MrDataset dataset);
	

}
