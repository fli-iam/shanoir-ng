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

import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * center service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class DatasetProcessingServiceImpl implements DatasetProcessingService {

	@Autowired
	private DatasetProcessingRepository repository;

	protected DatasetProcessing updateValues(final DatasetProcessing from, final DatasetProcessing to) {
		to.setDatasetProcessingType(from.getDatasetProcessingType());
		to.setComment(from.getComment());
		to.setInputDatasets(from.getInputDatasets());
		to.setOutputDatasets(from.getOutputDatasets());
		to.setProcessingDate(from.getProcessingDate());
		to.setStudyId(from.getStudyId());
		return to;
	}

	public Optional<DatasetProcessing> findByComment(String comment) {
		return repository.findByComment(comment);
	}
	
    @Override
    public Optional<DatasetProcessing> findById(final Long id) {
        return repository.findById(id);
    }
    
    @Override
    public List<DatasetProcessing> findAll() {
        return Utils.toList(repository.findAll());
    }
    
    @Override
    public DatasetProcessing create(final DatasetProcessing entity) {
        DatasetProcessing savedEntity = repository.save(entity);
        return savedEntity;
    }
    
    @Override
    public DatasetProcessing update(final DatasetProcessing entity) throws EntityNotFoundException {
        final Optional<DatasetProcessing> entityDbOpt = repository.findById(entity.getId());
        final DatasetProcessing entityDb = entityDbOpt.orElseThrow(
                () -> new EntityNotFoundException(entity.getClass(), entity.getId()));
        updateValues(entity, entityDb);
        return repository.save(entityDb);
    }

    @Override
    public void deleteById(final Long id) throws EntityNotFoundException  {
        final Optional<DatasetProcessing> entity = repository.findById(id);
        entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
        repository.deleteById(id);
    }

    /**
     * Unlink given dataset to all dataset processing
     *
     * @param datasetId
     */
    @Override
    public void removeDatasetFromAllInput(Long datasetId) {
        List<DatasetProcessing> processings = repository.findAllByInputDatasets_Id(datasetId);

        for(DatasetProcessing processing : processings){
            processing.getInputDatasets().removeIf(ds -> ds.getId().equals(datasetId));
        }

        repository.saveAll(processings);

    }
}
