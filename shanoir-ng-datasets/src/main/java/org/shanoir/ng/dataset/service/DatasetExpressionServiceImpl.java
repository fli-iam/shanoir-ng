package org.shanoir.ng.dataset.service;

import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.repository.DatasetExpressionRepository;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.datasetfile.DatasetFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DatasetExpressionServiceImpl implements DatasetExpressionService {

    @Autowired
    private DatasetExpressionRepository repository;

    @Autowired
    private EntityManager em;

    @Transactional(readOnly = true)
    public List<DatasetFile> getDatasetFiles(DatasetExpression datasetExpression) {
        if (em.contains(datasetExpression)) {
            Hibernate.initialize(datasetExpression.getDatasetFiles());
            return datasetExpression.getDatasetFiles();
        }
        datasetExpression = repository.findWithDatasetFiles(datasetExpression.getId());
        return datasetExpression.getDatasetFiles();
    }
}
