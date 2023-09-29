package org.shanoir.ng.processing.carmin.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.processing.carmin.model.ExecutionMonitoring;
import org.shanoir.ng.processing.carmin.model.ProcessingResource;
import org.shanoir.ng.processing.carmin.repository.ProcessingResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProcessingResourceServiceImpl implements ProcessingResourceService {

    @Autowired
    public ProcessingResourceRepository repository;
    @Autowired
    public DatasetService datasetService;

    @Override
    public List<Dataset> findDatasetsByResourceId(String resourceId) {
        List<Long> ids = repository.findDatasetIdsByResourceId(resourceId);
        return datasetService.findByIdIn(ids);
    }

    @Override
    public List<Long> findDatasetIdsByResourceId(String resourceId) {
        return repository.findDatasetIdsByResourceId(resourceId);
    }

    @Override
    public String create(ExecutionMonitoring processing, List<Dataset> datasets) {
        List<ProcessingResource> processingResources = new ArrayList<>();
        String resourceId = UUID.randomUUID().toString();
        for(Dataset dataset : datasets){
            processingResources.add(new ProcessingResource(processing, dataset, resourceId));
        }
        repository.saveAll(processingResources);
        return resourceId;
    }
}
