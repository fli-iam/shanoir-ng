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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.shanoir.ng.vip.processingResource.service.ProcessingResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private ProcessingResourceRepository processingResourceRepository;

    @Autowired
    private ProcessingResourceService processingResourceService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private SolrService solrService;

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

    public List<DatasetProcessing> findAllById(List<Long> idList) {
        return idList.stream().flatMap(it -> findById(it).stream()).toList();
    }

    @Override
    public List<DatasetProcessing> findByInputDatasetId(Long datasetId) {
        return repository.findAllByInputDatasets_Id(datasetId);
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
    @Transactional
    public void deleteById(final Long id) throws ShanoirException, RestServiceException, SolrServerException, IOException {
        final Optional<DatasetProcessing> entity = repository.findById(id);
        entity.orElseThrow(() -> new EntityNotFoundException("Cannot find dataset processing [" + id + "]"));

        // delete associated ressources
        processingResourceRepository.deleteByProcessingId(id);

        for (Dataset ds : entity.get().getOutputDatasets()) {
            datasetService.deleteById(ds.getId());
            solrService.deleteFromIndex(ds.getId());
        }

        this.deleteByParentId(id);
        repository.deleteById(id);
    }

    /**
     * Unlink given dataset to all dataset processing
     *
     * @param datasetId
     */
    @Override
    public void removeDatasetFromAllProcessingInput(Long datasetId) throws ShanoirException, RestServiceException, SolrServerException, IOException {
        List<DatasetProcessing> processings = repository.findAllByInputDatasets_Id(datasetId);
        List<DatasetProcessing> toUpdate = new ArrayList<>();
        List<DatasetProcessing> toDelete = new ArrayList<>();

        for (DatasetProcessing processing : processings) {
            processing.getInputDatasets().removeIf(ds -> ds.getId().equals(datasetId));
            if (processing.getInputDatasets().isEmpty()) {
                // If processing is no more linked to a dataset, delete it
                toDelete.add(processing);
            } else {
                toUpdate.add(processing);
            }
        }
        for (DatasetProcessing proc : toDelete) {
            this.deleteById(proc.getId());
        }
        repository.saveAll(toUpdate);
    }

    @Override
    public void deleteByParentId(Long id) throws ShanoirException, RestServiceException, SolrServerException, IOException {
        List<DatasetProcessing> processings = repository.findAllByParentId(id);
        for (DatasetProcessing child : processings) {
            this.deleteById(child.getId());
        }
    }

    @Override
    public void validateDatasetProcessing(DatasetProcessing processing) throws RestServiceException {
        if (processing.getStudyId() == null) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Processing must be linked to a study.", null);
            throw new RestServiceException(error);
        }
        if (processing.getInputDatasets() == null || processing.getInputDatasets().isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "There must be at least one input dataset.", null);
            throw new RestServiceException(error);
        }
        for (Dataset dataset : processing.getInputDatasets()) {
            if (!processing.getStudyId().equals(datasetService.getStudyId(dataset))) {
                ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Input dataset [" + dataset.getId() + "] is not linked to the processing study.", null);
                throw new RestServiceException(error);
            }
        }
    }
}
