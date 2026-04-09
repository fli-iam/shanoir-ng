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

    @Override
    public List<DatasetProperty> getByDatasetId(Long id) {
        return repository.getByDatasetId(id);
    }

    @Override
    public List<DatasetProperty> getByDatasetProcessingId(Long id) {
        return repository.getByProcessingId(id);
    }

    @Override
    public boolean existsById(Long processingId) {
        return repository.existsById(processingId);
    }

}
