
package org.shanoir.ng.property.service;

import org.shanoir.ng.property.model.DatasetProperty;

import java.util.List;

public interface DatasetPropertyService {

    List<DatasetProperty> createAll(List<DatasetProperty> properties);

    void deleteByDatasetId(Long id);

    List<DatasetProperty> getByDatasetId(Long id);

    List<DatasetProperty> getByDatasetProcessingId(Long id);

    boolean existsById(Long processingId);
}
