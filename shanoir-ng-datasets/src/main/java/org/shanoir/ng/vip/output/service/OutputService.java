package org.shanoir.ng.vip.output.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.handler.OutputHandler;

public interface OutputService {

    /**
     * Process the results of the given execution monitoring
     *
     * @param monitoring the execution monitoring relative to the resutls to process
     * @throws ResultHandlerException
     * @throws EntityNotFoundException
     */
    void process(ExecutionMonitoring monitoring) throws ResultHandlerException, EntityNotFoundException;

    /**
     * Process the results of the given execution monitoring with the given output handler
     *
     * @param monitoring the execution monitoring relative to the resutls to process
     * @param relevantOutputHandler the output handler to use for the type of the execution monitoring
     * @throws ResultHandlerException
     * @throws EntityNotFoundException
     */
    void process(ExecutionMonitoring monitoring, OutputHandler relevantOutputHandler) throws ResultHandlerException, EntityNotFoundException;
}
