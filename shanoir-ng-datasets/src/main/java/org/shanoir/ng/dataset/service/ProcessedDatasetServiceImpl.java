package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessedDatasetServiceImpl implements ProcessedDatasetService {

    @Autowired
    private DatasetRepository repository;


    @Override
    public List<Dataset> deleteByProcessingId(Long id) {
        return repository.deleteByDatasetProcessingId(id);
    }
}
