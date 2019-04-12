package org.shanoir.ng.datasetacquisition.dto.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

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
