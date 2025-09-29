package org.shanoir.ng.vip.output.handler;

import org.shanoir.ng.vip.executionMonitoring.service.ExecutionTrackingServiceImpl;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.util.List;

public abstract class OutputHandler {

    @Autowired
    protected ExecutionTrackingServiceImpl executionTrackingService;

    /**
     * Return true if the implementation can process the result of the given processing
     *
     * @param monitoring ExecutionMonitoring
     * @return true if execution monitoring can be process by this handler instance
     */
    public abstract boolean canProcess(ExecutionMonitoring monitoring) throws ResultHandlerException;

    /**
     * This methods manages the single result of an execution
     *
     * @param resultFiles  the result file as tar.gz of the processing
     * @param parentFolder the temporary arent folder in which we are currently working
     * @param processing   the corresponding dataset processing.
     */
    public abstract void manageTarGzResult(List<File> resultFiles, File parentFolder, ExecutionMonitoring processing) throws ResultHandlerException;
}
