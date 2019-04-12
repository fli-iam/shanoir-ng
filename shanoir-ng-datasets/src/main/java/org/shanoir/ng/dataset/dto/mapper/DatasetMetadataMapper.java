package org.shanoir.ng.dataset.dto.mapper;

import org.mapstruct.Mapper;
import org.shanoir.ng.dataset.dto.DatasetMetadataDTO;
import org.shanoir.ng.dataset.model.DatasetMetadata;

/**
 * Mapper for dataset metadata.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
public interface DatasetMetadataMapper {

	/**
	 * Map a @DatasetMetadata to a @DatasetMetadataDTO.
	 * 
	 * @param datasets
	 *            dataset.
	 * @return dataset DTO.
	 */
	DatasetMetadataDTO datasetMetadataToDatasetMetadataDTO(DatasetMetadata dataset);


}
