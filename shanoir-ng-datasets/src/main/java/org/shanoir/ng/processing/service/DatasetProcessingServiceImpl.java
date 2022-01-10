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

package org.shanoir.ng.processing.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.springframework.stereotype.Service;

/**
 * center service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class DatasetProcessingServiceImpl extends BasicEntityServiceImpl<DatasetProcessing> implements DatasetProcessingService {

	@Autowired
	private DatasetProcessingRepository datasetProcessingRepository;

	@Override
	protected DatasetProcessing updateValues(final DatasetProcessing from, final DatasetProcessing to) {
		to.setDatasetProcessingType(from.getDatasetProcessingType());
		to.setComment(from.getComment());
		to.setInputDatasets(from.getInputDatasets());
		to.setOutputDatasets(from.getOutputDatasets());
		to.setProcessingDate(from.getProcessingDate());
		to.setStudyId(from.getStudyId());
		return to;
	}

	@Override
	public Optional<DatasetProcessing> findByComment(String comment) {
		return datasetProcessingRepository.findByComment(comment);
	}
}
