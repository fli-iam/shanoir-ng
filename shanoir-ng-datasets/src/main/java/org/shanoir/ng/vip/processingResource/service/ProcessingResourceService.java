package org.shanoir.ng.vip.processingResource.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;

import java.util.List;

public interface ProcessingResourceService {

    List<Dataset> findDatasetsByResourceId(String resourceId);


    List<Long> findDatasetIdsByResourceId(String resourceId);

    String create(ExecutionMonitoring processing, List<Dataset> datasets);

    void deleteByProcessingId(Long processingId);

    void deleteByDatasetId(Long datasetId);
}
