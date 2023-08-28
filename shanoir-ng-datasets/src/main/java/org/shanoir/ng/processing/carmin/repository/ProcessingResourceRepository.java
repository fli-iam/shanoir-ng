package org.shanoir.ng.processing.carmin.repository;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.carmin.model.ProcessingResource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingResourceRepository  extends CrudRepository<ProcessingResource, Long> {

    List<Long> findDatasetIdByResourceId(String resourceId);
}
