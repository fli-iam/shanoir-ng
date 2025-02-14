package org.shanoir.ng.vip.processingResource.repository;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.vip.processingResource.model.ProcessingResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingResourceRepository  extends CrudRepository<ProcessingResource, Long> {

    @Query(value = "SELECT d FROM Dataset AS d JOIN ProcessingResource AS p ON d.id = p.dataset.id WHERE p.resourceId = :resourceId")
    List<Dataset> findDatasetsByResourceId(String resourceId);

    void deleteByProcessingId(Long processingId);

    void deleteByDatasetId(Long datasetId);
}
