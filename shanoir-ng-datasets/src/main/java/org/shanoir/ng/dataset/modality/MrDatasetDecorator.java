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

package org.shanoir.ng.dataset.modality;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;

/**
 * Decorator for dataset acquisitions mapper.
 * 
 * @author msimon
 *
 */
public abstract class MrDatasetDecorator implements MrDatasetMapper {

	@Autowired
	private MrDatasetMapper delegate;


	public MrDatasetDecorator() {
	}

	@Override
	public List<IdNameDTO> datasetsToIdNameDTOs(final List<MrDataset> datasets) {
		final List<IdNameDTO> datasetDTOs = new ArrayList<>();
		for (MrDataset dataset : datasets) {
			datasetDTOs.add(datasetToIdNameDTO(dataset));
		}
		return datasetDTOs;
	}

	@Override
	public IdNameDTO datasetToIdNameDTO(final MrDataset dataset) {
		return delegate.datasetToIdNameDTO(dataset);
	}

	@Override
	public PageImpl<MrDatasetDTO> datasetToDatasetDTO(Page<MrDataset> page) {
		org.springframework.data.domain.Page<MrDatasetDTO> mappedPage =  page.map(new Converter<MrDataset, MrDatasetDTO>() {
			@Override
			public MrDatasetDTO convert(MrDataset entity) {
				return delegate.datasetToDatasetDTO(entity);
			}
		});
		return new PageImpl<MrDatasetDTO>(mappedPage.getContent());
	}

}
