package org.shanoir.ng.vip.executionMonitoring.service;

import jakarta.servlet.http.HttpServletResponse;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;

public interface ExecutionTrackingService {

    /**
     * Create or update the tracking line relative to : the pipeline / the execution monitoring / the execution status
     *
     * @param executionMonitoring
     * @param execStatus
     */
    void updateTrackingFile(ExecutionMonitoring executionMonitoring, ExecutionTrackingServiceImpl.execStatus execStatus);

    /**
     * Complete the tracking line relative to : the pipeline / the execution monitoring / the new processing generated
     *
     * @param executionMonitoring
     * @param newProcessing
     */
    void completeTracking(ExecutionMonitoring executionMonitoring, DatasetProcessing newProcessing);

    /**
     * Download the tracking file relative to the given pipeline name and put it in the Http resposne as a .zip archive
     *
     * @param pipelineName
     * @param response
     */
    void downloadTrackingFile(String pipelineName, HttpServletResponse response) throws RestServiceException;
}
