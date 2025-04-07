package org.shanoir.ng.vip.output.service;

import org.apache.commons.collections4.ListUtils;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.execution.service.ExecutionServiceImpl;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.repository.ExecutionMonitoringRepository;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.handler.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PostProcessingServiceImpl implements PostProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionServiceImpl.class);

    private static final int THREAD_NUMBER = 10;

    private static OutputHandler relevantOutputHandler;

    @Autowired
    private List<OutputHandler> outputHandlers;

    @Autowired
    private OutputService outputService;

    @Autowired
    private ExecutionMonitoringRepository monitoringRepository;

    public void launchPostProcessing(List<Long> processingIds, String comment) throws ResultHandlerException {
        retrieveRelevantOutputHandler(comment);
        launchPostProcessing(processingIds);
    }

    /**
     * Retrieve relevant output handler relative to the given processing comment
     */
    private void retrieveRelevantOutputHandler(String comment) throws ResultHandlerException {
        for (OutputHandler outputHandler : outputHandlers) {
            if (outputHandler.canProcess(comment, true)) {
                relevantOutputHandler = outputHandler;
            }
        }
    }
    /**
     * Launch post processing for given processing ids with the static output handler
     */
    private void launchPostProcessing(List<Long> processingIds) {
        for (List<Long> partition : ListUtils.partition(processingIds, THREAD_NUMBER * 10)) {
            launchPostProcessingPartition(partition);
        }
        LOG.info("Post processing session finished.");
    }

    /**
     * Launch post processing for given processing ids partition with the static output handler
     */
    @Transactional
    protected void launchPostProcessingPartition(List<Long> partition) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);

        for (ExecutionMonitoring monitoring : monitoringRepository.findByDatasetProcessingIds(partition)) {
            executor.submit(() -> {
                try {
                    LOG.info("Post processing results of the processing : " + monitoring.getId());
                    outputService.process(monitoring, relevantOutputHandler);
                } catch (ResultHandlerException | EntityNotFoundException e) {
                    LOG.error("Post processing of monitoring name: {}, {} failed.", monitoring.getName(), monitoring.getId(), e);
                }
            });
        }

        executor.shutdown();
        try {
            while (!executor.isTerminated()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
