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
import java.util.stream.Collectors;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.ObjectFactory;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetUtils;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDatasetsDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.bids.BidsDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.eeg.EegDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = { ExaminationMapper.class, DatasetMapper.class })
@DecoratedWith(DatasetAcquisitionDatasetsDecorator.class)
@MapperConfig(mappingInheritanceStrategy=MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface DatasetAcquisitionDatasetsMapper {
	
	List<DatasetAcquisitionDatasetsDTO> datasetAcquisitionsToDatasetAcquisitionDatasetsDTOs(
			List<DatasetAcquisition> datasetAcquisitions);

	PageImpl<DatasetAcquisitionDatasetsDTO> datasetAcquisitionsToDatasetAcquisitionDatasetsDTOs(Page<DatasetAcquisition> daPage);
			
	DatasetAcquisitionDatasetsDTO datasetAcquisitionToDatasetAcquisitionDatasetsDTO(
			DatasetAcquisition datasetAcquisition);

	DatasetAcquisition datasetAcquisitionDatasetsDTOToDatasetAcquisition(DatasetAcquisitionDatasetsDTO dto);
	
	@ObjectFactory
	default DatasetAcquisition createDatasetAcquisition(DatasetAcquisitionDatasetsDTO dto) {
		if (dto.getType().equals("Mr")) return new MrDatasetAcquisition(); 
        else if (dto.getType().equals("Pet")) return new PetDatasetAcquisition(); 
        else if (dto.getType().equals("Ct")) return new CtDatasetAcquisition();
		else if (dto.getType().equals("BIDS")) return new BidsDatasetAcquisition();
		else if (dto.getType().equals("Eeg")) return new EegDatasetAcquisition();
		else if (dto.getType().equals("Xa")) return new XaDatasetAcquisition();
		else if (dto.getType().equals("Generic")) return new GenericDatasetAcquisition();
		else throw new IllegalStateException("Cannot map from a dataset acquisition dto that don't provide a valid type. Given type = " + dto.getType());
    }
	
	@ObjectFactory
	default Dataset createDataset(DatasetDTO dto) {
		Dataset ds = DatasetUtils.buildDatasetFromType(dto.getType());
		if (ds != null) return ds;
        else throw new IllegalStateException("Cannot map from a dataset dto that don't provide a valid type. Given type = " + dto.getType());
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
}
