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
import java.util.function.Function;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Decorator for dataset acquisitions mapper.
 * 
 * @author msimon
 *
 */
@Component
public abstract class EegDatasetDecorator implements EegDatasetMapper {

	@Autowired
	private EegDatasetMapper delegate;


	public EegDatasetDecorator() {
	}

	@Override
	public List<IdName> datasetsToIdNameDTOs(final List<EegDataset> datasets) {
		final List<IdName> datasetDTOs = new ArrayList<>();
		for (EegDataset dataset : datasets) {
			datasetDTOs.add(datasetToIdNameDTO(dataset));
		}
		return datasetDTOs;
	}

	@Override
	public IdName datasetToIdNameDTO(final EegDataset dataset) {
		return delegate.datasetToIdNameDTO(dataset);
	}

	@Override
	public PageImpl<EegDatasetDTO> datasetToDatasetDTO(Page<EegDataset> page) {
		org.springframework.data.domain.Page<EegDatasetDTO> mappedPage =  page.map(new Function<EegDataset, EegDatasetDTO>() {
			public EegDatasetDTO apply(EegDataset entity) {
				return delegate.datasetToDatasetDTO(entity);
			}
		});
		return new PageImpl<>(mappedPage.getContent());
	}

}
