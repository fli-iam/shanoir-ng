package org.shanoir.ng.dataset;

import org.mapstruct.Mapper;

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
