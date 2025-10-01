package org.shanoir.ng.vip.execution.service;

import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;

public interface ExecutionTrackingService {

    /**
     * Create or update the tracking line relative to : the pipeline / the execution monitoring / the execution status
     *
     * @param executionMonitoring
     * @param execStatus
     */
    void updateTrackingFile(ExecutionMonitoring executionMonitoring, ExecutionTrackingServiceImpl.ExecStatus execStatus);

    /**
     * Complete the tracking line relative to : the pipeline / the execution monitoring / the new processing generated
     *
     * @param executionMonitoring
     * @param newProcessing
     */
    void completeTracking(ExecutionMonitoring executionMonitoring, DatasetProcessing newProcessing);
}
