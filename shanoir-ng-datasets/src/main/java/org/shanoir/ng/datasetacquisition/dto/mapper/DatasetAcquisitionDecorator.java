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

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

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
	public List<ExaminationDatasetAcquisitionDTO> datasetAcquisitionsToExaminationDatasetAcquisitionDTOs(
			final List<DatasetAcquisition> datasetAcquisitions) {
		if (datasetAcquisitions == null) {
			return null;
		}
		final List<ExaminationDatasetAcquisitionDTO> datasetAcquisitionDTOs = new ArrayList<>();
		for (DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
			datasetAcquisitionDTOs.add(datasetAcquisitionToExaminationDatasetAcquisitionDTO(datasetAcquisition));
		}
		return datasetAcquisitionDTOs;
	}

	@Override
	public ExaminationDatasetAcquisitionDTO datasetAcquisitionToExaminationDatasetAcquisitionDTO(
			final DatasetAcquisition datasetAcquisition) {
		final ExaminationDatasetAcquisitionDTO datasetAcquisitionDTO = delegate
				.datasetAcquisitionToExaminationDatasetAcquisitionDTO(datasetAcquisition);
		datasetAcquisitionDTO.setName(getExaminationDatasetAcquisitionDTOName(datasetAcquisition));

		return datasetAcquisitionDTO;
	}

	/**
	 * Get dataset acquisition name. If all the datasets have the same name,
	 * then return the name of the datasets. Else if all the datasets have the
	 * same comment, then return the comment of the datasets.
	 * 
	 * @param datasetAcquisition dataset acquisition.
	 * @return name.
	 */
	private String getExaminationDatasetAcquisitionDTOName(final DatasetAcquisition datasetAcquisition) {
		final StringBuilder result = new StringBuilder();
		final List<String> datasetNameSet = new ArrayList<>();
		final List<String> datasetCommentSet = new ArrayList<>();
		if (datasetAcquisition.getDatasets() != null) {
			for (final Dataset dataset : datasetAcquisition.getDatasets()) {
				final String datasetName = dataset.getName();
				if (!StringUtils.isEmpty(datasetName) && !datasetNameSet.contains(datasetName)) {
					datasetNameSet.add(datasetName);
				}
				String datasetComment = dataset.getOriginMetadata().getComment();
				if (dataset.getUpdatedMetadata() != null && dataset.getUpdatedMetadata().getComment() != null) {
					datasetComment = dataset.getUpdatedMetadata().getComment();
				}
				if (!StringUtils.isEmpty(datasetComment) && !datasetCommentSet.contains(datasetComment)) {
					datasetCommentSet.add(datasetComment);
				}
			}
		}

		if (datasetNameSet.size() == 1) {
			result.append(datasetNameSet.get(0));
		} else if (datasetCommentSet.size() == 1) {
			result.append(datasetCommentSet.get(0));
		} else if (datasetNameSet.size() > 1) {
			for (final String name : datasetNameSet) {
				result.append(name).append(" ");
			}
			result.deleteCharAt(result.length() - 1);
		} else {
			result.append("id=").append(datasetAcquisition.getId());
			if (datasetAcquisition.getRank() != null) {
				result.append(" rank=").append(datasetAcquisition.getRank());
			}
		}
		// TODO: retrieve dataset acquisition type
		final String type = "";

		return result.append(" (").append(type).append(")").toString();
	}

}
