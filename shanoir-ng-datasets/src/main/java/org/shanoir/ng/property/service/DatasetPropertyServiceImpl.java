package org.shanoir.ng.property.service;

import org.shanoir.ng.property.model.DatasetProperty;
import org.shanoir.ng.property.repository.DatasetPropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetPropertyServiceImpl implements DatasetPropertyService {

    @Autowired
    private DatasetPropertyRepository repository;
    @Override
    public List<DatasetProperty> createAll(List<DatasetProperty> properties) {
        repository.saveAll(properties);
        return properties;
    }

    @Override
    public void deleteByDatasetId(Long id) {
        repository.deleteByDatasetId(id);
    }
}