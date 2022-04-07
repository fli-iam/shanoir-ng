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

import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * Decorator for dataset acquisitions mapper.
 * 
 * @author msimon
 *
 */
public abstract class DatasetAcquisitionDecorator implements DatasetAcquisitionMapper {

	@Autowired
	private DatasetAcquisitionMapper delegate;

	
	@Override
	public List<DatasetAcquisitionDTO> datasetAcquisitionsToDatasetAcquisitionDTOs(
			final List<DatasetAcquisition> datasetAcquisitions) {
		if (datasetAcquisitions == null) {
			return null;
		}
		final List<DatasetAcquisitionDTO> datasetAcquisitionDTOs = new ArrayList<>();
		for (DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
			datasetAcquisitionDTOs.add(datasetAcquisitionToDatasetAcquisitionDTO(datasetAcquisition));
		}
		return datasetAcquisitionDTOs;
	}
	
	@Override
	public PageImpl<DatasetAcquisitionDTO> datasetAcquisitionsToDatasetAcquisitionDTOs(Page<DatasetAcquisition> page) {
		Page<DatasetAcquisitionDTO> mappedPage = page.map(new Function<DatasetAcquisition, DatasetAcquisitionDTO>() {
			public DatasetAcquisitionDTO apply(DatasetAcquisition entity) {
				return datasetAcquisitionToDatasetAcquisitionDTO(entity);
			}
		});
		return new PageImpl<>(mappedPage);
	}

	@Override
	public DatasetAcquisitionDTO datasetAcquisitionToDatasetAcquisitionDTO(
			final DatasetAcquisition datasetAcquisition) {
		if (datasetAcquisition == null) {
			return null;
		}
		final DatasetAcquisitionDTO datasetAcquisitionDTO = delegate
				.datasetAcquisitionToDatasetAcquisitionDTO(datasetAcquisition);
        if (datasetAcquisition.getType().equals("Mr")) {
        	datasetAcquisitionDTO.setProtocol(((MrDatasetAcquisition)datasetAcquisition).getMrProtocol());
        } else if (datasetAcquisition.getType().equals("Pet")) {
        	datasetAcquisitionDTO.setProtocol(((PetDatasetAcquisition)datasetAcquisition).getPetProtocol());
        } else if (datasetAcquisition.getType().equals("Ct")) {
        	datasetAcquisitionDTO.setProtocol(((CtDatasetAcquisition)datasetAcquisition).getCtProtocol());
        }

		return datasetAcquisitionDTO;
	}
}
