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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.mapstruct.Mapping;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDatasetsDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * Decorator for dataset acquisitions mapper.
 *
 * @author msimon
 *
 */
public abstract class DatasetAcquisitionDatasetsDecorator implements DatasetAcquisitionDatasetsMapper {

	@Autowired
	private DatasetAcquisitionDatasetsMapper delegate;

	@Override
	public List<DatasetAcquisitionDatasetsDTO> datasetAcquisitionsToDatasetAcquisitionDatasetsDTOs(
			final List<DatasetAcquisition> datasetAcquisitions) {
		if (datasetAcquisitions == null) {
			return null;
		}
		final List<DatasetAcquisitionDatasetsDTO> datasetAcquisitionDTOs = new ArrayList<>();
		for (DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
			datasetAcquisitionDTOs.add(datasetAcquisitionToDatasetAcquisitionDatasetsDTO(datasetAcquisition));
		}
		return datasetAcquisitionDTOs;
	}

	@Override
	@Mapping(source = "dsAcqPage", target = "dsAcqWDTOsPage")
	public PageImpl<DatasetAcquisitionDatasetsDTO> datasetAcquisitionsToDatasetAcquisitionDatasetsDTOs(
			final Page<DatasetAcquisition> page) {

		Page<DatasetAcquisitionDatasetsDTO> mappedPage = page
				.map(new Function<DatasetAcquisition, DatasetAcquisitionDatasetsDTO>() {
					public DatasetAcquisitionDatasetsDTO apply(DatasetAcquisition entity) {
						return datasetAcquisitionToDatasetAcquisitionDatasetsDTO(entity);
					}
				});
		return new PageImpl<>(mappedPage);
	}

	@Override
	public DatasetAcquisitionDatasetsDTO datasetAcquisitionToDatasetAcquisitionDatasetsDTO(
			final DatasetAcquisition datasetAcquisition) {
		if (datasetAcquisition == null) {
			return null;
		}
		final DatasetAcquisitionDatasetsDTO datasetAcquisitionDTO = delegate
				.datasetAcquisitionToDatasetAcquisitionDatasetsDTO(datasetAcquisition);
		setType(datasetAcquisitionDTO, datasetAcquisition);
		return datasetAcquisitionDTO;
	}

	private void setType(DatasetAcquisitionDTO datasetAcquisitionDTO, DatasetAcquisition datasetAcquisition) {
		if (datasetAcquisition.getType().equals("Mr")) {
			datasetAcquisitionDTO.setProtocol(((MrDatasetAcquisition) datasetAcquisition).getMrProtocol());
		} else if (datasetAcquisition.getType().equals("Pet")) {
			datasetAcquisitionDTO.setProtocol(((PetDatasetAcquisition) datasetAcquisition).getPetProtocol());
		} else if (datasetAcquisition.getType().equals("Ct")) {
			datasetAcquisitionDTO.setProtocol(((CtDatasetAcquisition) datasetAcquisition).getCtProtocol());
		} else if (datasetAcquisition.getType().equals("Xa")) {
			datasetAcquisitionDTO.setProtocol(((XaDatasetAcquisition) datasetAcquisition).getXaProtocol());
		}
	}
}
