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

package org.shanoir.ng.dataset.dto.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.DatasetWithDependenciesDTO;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.StudyTagDTOLight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * Decorator for dataset acquisitions mapper.
 * 
 * @author msimon
 * @author jlouis
 *
 */
public abstract class DatasetDecorator implements DatasetMapper {

	@Autowired
	private DatasetMapper defaultMapper;

	@Autowired
	private MrDatasetMapper mrMapper;

	@Autowired
	protected EegDatasetMapper eegMapper;

	@Autowired
	protected DatasetMetadataMapper datasetMetadataMapper;

	@Autowired
	protected DatasetAcquisitionMapper acquisitionMapper;

	@Override
	public List<IdName> datasetsToIdNameDTOs(final List<Dataset> datasets) {
		final List<IdName> datasetDTOs = new ArrayList<>();
		for (Dataset dataset : datasets) {
			datasetDTOs.add(datasetToIdNameDTO(dataset));
		}
		return datasetDTOs;
	}

	@Override
	public PageImpl<DatasetDTO> datasetToDatasetDTO(Page<Dataset> page) {
		Page<DatasetDTO> mappedPage = page.map(new Function<Dataset, DatasetDTO>() {
			public DatasetDTO apply(Dataset entity) {
				if (entity instanceof MrDataset) {
					return mrMapper.datasetToDatasetDTO((MrDataset)entity);
				}
				else if (entity instanceof EegDataset) {
					return eegMapper.datasetToDatasetDTO((EegDataset)entity);
				}
				else {
					return defaultMapper.datasetToDatasetDTO(entity);
				}
			}
		});
		return new PageImpl<>(mappedPage);
	}

	@Override
	public IdName datasetToIdNameDTO(final Dataset dataset) {
		return defaultMapper.datasetToIdNameDTO(dataset);
	}

	@Override
	public DatasetWithDependenciesDTO datasetToDatasetWithParentsAndProcessingsDTO(Dataset dataset) {
		final DatasetWithDependenciesDTO datasetDTO = defaultMapper.datasetToDatasetWithParentsAndProcessingsDTO(dataset);
		datasetDTO.setDownloadPath(dataset.getDownloadPath());
		Hibernate.initialize(dataset.getCopies());
		datasetDTO.setCopies(dataset.getCopies().stream()
				.map(Dataset::getId)
				.collect(Collectors.toList()));
		return datasetDTO;
	}

	protected List<StudyTagDTOLight> studyTagListToStudyTagDTOLightList(List<StudyTag> list) {
		if ( list == null ) {
			return null;
		}

		List<StudyTagDTOLight> list1 = new ArrayList<StudyTagDTOLight>( list.size() );
		for ( StudyTag studyTag : list ) {
			list1.add( studyTagToStudyTagDTOLight( studyTag ) );
		}

		return list1;
	}

	protected StudyTagDTOLight studyTagToStudyTagDTOLight(StudyTag studyTag) {
		if ( studyTag == null ) {
			return null;
		}

		StudyTagDTOLight studyTagDTOLight = new StudyTagDTOLight();

		studyTagDTOLight.setId( studyTag.getId() );
		studyTagDTOLight.setName( studyTag.getName() );
		studyTagDTOLight.setColor( studyTag.getColor() );

		return studyTagDTOLight;
	}
}
