package org.shanoir.ng.dataset;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Mapper for datasets.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
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
	 * Map a @Dataset to a @IdNameDTO.
	 * 
	 * @param dataset
	 *            dataset to map.
	 * @return dataset DTO.
	 */
	@Mappings({ @Mapping(target = "name", ignore = true) })
	IdNameDTO datasetToIdNameDTO(Dataset dataset);

}
