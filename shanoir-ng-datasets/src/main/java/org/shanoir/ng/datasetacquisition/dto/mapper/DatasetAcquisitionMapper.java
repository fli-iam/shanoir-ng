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

package org.shanoir.ng.datasetacquisition.dto.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.ObjectFactory;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = { ExaminationMapper.class })
@DecoratedWith(DatasetAcquisitionDecorator.class)
@MapperConfig(mappingInheritanceStrategy=MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface DatasetAcquisitionMapper {

	
	List<DatasetAcquisitionDTO> datasetAcquisitionsToDatasetAcquisitionDTOs(
			List<DatasetAcquisition> datasetAcquisitions); 
	
	
	public PageImpl<DatasetAcquisitionDTO> datasetAcquisitionsToDatasetAcquisitionDTOs(Page<DatasetAcquisition> page);

	
	DatasetAcquisitionDTO datasetAcquisitionToDatasetAcquisitionDTO(
			DatasetAcquisition datasetAcquisition);

	
	DatasetAcquisition datasetAcquisitionDTOToDatasetAcquisition(DatasetAcquisitionDTO datasetAcquisition);
	
	@ObjectFactory
	default DatasetAcquisition createDatasetAcquisition(DatasetAcquisitionDTO dto) {
        if (dto.getType().equals("Mr")) return new MrDatasetAcquisition(); 
        else if (dto.getType().equals("Pet")) return new PetDatasetAcquisition(); 
        else if (dto.getType().equals("Ct")) return new CtDatasetAcquisition(); 
        else throw new IllegalStateException("Cannot map from a dataset acquisition dto that don't provide a valid type. Given type = " + dto.getType());
    }

}
