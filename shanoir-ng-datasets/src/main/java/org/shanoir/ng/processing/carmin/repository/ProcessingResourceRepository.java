package org.shanoir.ng.processing.carmin.repository;

import org.shanoir.ng.processing.carmin.model.ProcessingResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingResourceRepository  extends CrudRepository<ProcessingResource, Long> {

    @Query(value = "SELECT dataset.id FROM ProcessingResource WHERE resourceId = :resourceId")
    List<Long> findDatasetIdsByResourceId(String resourceId);
}
