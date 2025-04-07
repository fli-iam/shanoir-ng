package org.shanoir.ng.vip.output.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.handler.OutputHandler;

public interface OutputService {

    /**
     * Set up the execution result of the given execution for management and potential post processing
     *
     * @param monitoring
     * @throws ResultHandlerException
     */
    void process(ExecutionMonitoring monitoring, OutputHandler outputHandler) throws ResultHandlerException, EntityNotFoundException;
}
