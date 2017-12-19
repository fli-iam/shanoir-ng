package org.shanoir.ng.dataset;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Decorator for dataset acquisitions mapper.
 * 
 * @author msimon
 *
 */
public abstract class DatasetDecorator implements DatasetMapper {

	private static final DateFormat shortDateFormatEN = DateFormat.getDateInstance(DateFormat.SHORT,
			new Locale("FR", "fr"));

	@Autowired
	private DatasetMapper delegate;

	@Override
	public List<IdNameDTO> datasetsToIdNameDTOs(final List<Dataset> datasets) {
		final List<IdNameDTO> datasetDTOs = new ArrayList<>();
		for (Dataset dataset : datasets) {
			datasetDTOs.add(datasetToIdNameDTO(dataset));
		}
		return datasetDTOs;
	}

	@Override
	public IdNameDTO datasetToIdNameDTO(final Dataset dataset) {
		final IdNameDTO datasetDTO = delegate.datasetToIdNameDTO(dataset);
		datasetDTO.setName(getDatasetDTOName(dataset));

		return datasetDTO;
	}

	/*
	 * Get dataset name. If name is not present, returns
	 * "[id] [creation date] [type]".
	 * 
	 * @param dataset dataset.
	 * 
	 * @return name.
	 */
	private String getDatasetDTOName(final Dataset dataset) {
		if (!StringUtils.isEmpty(dataset.getName())) {
			return dataset.getName();
		} else {
			final StringBuilder result = new StringBuilder();
			result.append(dataset.getId());
			if (dataset.getCreationDate() != null) {
				result.append(" ").append(shortDateFormatEN.format(dataset.getCreationDate()));
			}
			result.append(" ").append(dataset.getDatasetModalityType().name().split("_")[0]);
			return result.toString();
		}
	}

}
