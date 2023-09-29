package org.shanoir.ng.processing.carmin.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.carmin.model.ExecutionMonitoring;

import java.util.List;

public interface ProcessingResourceService {

    List<Dataset> findDatasetsByResourceId(String resourceId);


    List<Long> findDatasetIdsByResourceId(String resourceId);

    String create(ExecutionMonitoring processing, List<Dataset> datasets);
}
