package org.shanoir.ng.property.repository;

import org.shanoir.ng.property.model.DatasetProperty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetPropertyRepository extends CrudRepository<DatasetProperty, Long> {

    void deleteByDatasetId(Long id);

    List<DatasetProperty> getByDatasetId(Long id);

    List<DatasetProperty> getByProcessingId(Long id);
}