package org.shanoir.ng.vip.processingResource.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;

import java.util.List;

public interface ProcessingResourceService {

    /**
     * Create and save all resources objects relative to an execution monitoring under a unique resource id
     *
     * @return the resource Id
     */
    String create(ExecutionMonitoring processing, List<Dataset> datasets);
}
