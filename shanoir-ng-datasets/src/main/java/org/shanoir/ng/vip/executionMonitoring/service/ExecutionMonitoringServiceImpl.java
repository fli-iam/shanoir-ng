/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.vip.executionMonitoring.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.dto.VipExecutionDTO;
import org.shanoir.ng.vip.execution.service.ExecutionServiceImpl;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.repository.ExecutionMonitoringRepository;
import org.shanoir.ng.vip.executionMonitoring.security.ExecutionMonitoringSecurityService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.service.OutputService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Exceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author KhalilKes
 */
@Service
@EnableAsync
public class ExecutionMonitoringServiceImpl implements ExecutionMonitoringService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final float DEFAULT_PROGRESS = 0.5f;
    @Value("${vip.sleep-time}")
    private long sleepTime;
    private ThreadLocal<Boolean> stop = new ThreadLocal<>();
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionMonitoringServiceImpl.class);
    private static final String RIGHT_STR = "CAN_SEE_ALL";

    @Autowired
    private ExecutionMonitoringRepository repository;

    @Autowired
    private ExecutionMonitoringSecurityService executionMonitoringSecurityService;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private ExecutionServiceImpl executionService;

    @Autowired
    private OutputService outputService;

    public ExecutionMonitoring createExecutionMonitoring(ExecutionCandidateDTO execution, List<Dataset> inputDatasets) throws RestServiceException {
        ExecutionMonitoring executionMonitoring = new ExecutionMonitoring();
        executionMonitoring.setName(execution.getName());
        executionMonitoring.setPipelineIdentifier(execution.getPipelineIdentifier());
        executionMonitoring.setResultsLocation(KeycloakUtil.getTokenUserId() + "/" + formatter.format(LocalDateTime.now()));
        executionMonitoring.setTimeout(20);
        executionMonitoring.setStudyId(execution.getStudyIdentifier());
        executionMonitoring.setStatus(ExecutionStatus.RUNNING);
        executionMonitoring.setComment(execution.getName());
        executionMonitoring.setDatasetProcessingType(DatasetProcessingType.valueOf(execution.getProcessingType()));
        executionMonitoring.setOutputProcessing(execution.getOutputProcessing());
        executionMonitoring.setInputDatasets(inputDatasets);
        executionMonitoring.setUsername(KeycloakUtil.getTokenUserName());
        datasetProcessingService.validateDatasetProcessing(executionMonitoring);
        return repository.save(executionMonitoring);
    }

    public ExecutionMonitoring update(final ExecutionMonitoring executionMonitoring) throws EntityNotFoundException {
        final Optional<ExecutionMonitoring> entityDbOpt = repository
                .findById(executionMonitoring.getId());
        final ExecutionMonitoring entityDb = entityDbOpt.orElseThrow(
                () -> new EntityNotFoundException(executionMonitoring.getClass(),
                        executionMonitoring.getId()));

        updateValues(executionMonitoring, entityDb);
        return repository.save(entityDb);
    }


    public List<ExecutionMonitoring> findAllAllowed() {
        return executionMonitoringSecurityService.filterExecutionMonitoringList(Utils.toList(repository.findAll()), RIGHT_STR);
    }

    @Async("asyncExecutor")
    @Transactional
    public void startMonitoringJob(ExecutionMonitoring processing, ShanoirEvent event) {
        int attempts = 1;
        String identifier = processing.getIdentifier();

        stop.set(false);

        String execLabel = getExecLabel(processing);
        event = initShanoirEvent(processing, event, execLabel);

        while (!stop.get()) {

            try {
                VipExecutionDTO dto = executionService.getExecutionAsServiceAccount(attempts, identifier).block();

                if (dto == null) {
                    attempts++;
                    continue;
                } else {
                    attempts = 1;
                }
                switch (dto.getStatus()) {
                    case FINISHED -> processFinishedJob(processing, event, dto.getEndDate());
                    case UNKNOWN, EXECUTION_FAILED, KILLED -> processKilledJob(processing, event, dto);
                    case RUNNING -> {
                        try {
                            Thread.sleep(sleepTime); // sleep/stop a thread for 20 seconds
                        } catch (InterruptedException e) {
                            event.setMessage(execLabel + " : Monitoring interrupted, current state unknown...");
                            eventService.publishEvent(event);
                            LOG.warn("Execution monitoring thread interrupted", e);
                        }
                    }
                    default -> stop.set(true);
                }
            } catch (Exception e) {
                // Unwrap ReactiveException thrown from async method
                Throwable ex = Exceptions.unwrap(e);
                LOG.error(ex.getMessage(), ex.getCause());
                setEventInError(event, execLabel + " : " + ex.getMessage());
                LOG.warn("Stopping thread...");
                stop.set(true);
            }
        }
    }

    /**
     * Update values of an existing execution monitoring. No saving.
     */
    private void updateValues(ExecutionMonitoring from, ExecutionMonitoring to) {
        to.setIdentifier(from.getIdentifier());
        to.setStatus(from.getStatus());
        to.setName(from.getName());
        to.setPipelineIdentifier(from.getPipelineIdentifier());
        to.setStartDate(from.getStartDate());
        to.setEndDate(from.getEndDate());
        to.setTimeout(from.getTimeout());
        to.setResultsLocation(from.getResultsLocation());

        to.setDatasetProcessingType(from.getDatasetProcessingType());
        to.setComment(from.getComment());
        to.setInputDatasets(from.getInputDatasets());
        to.setOutputDatasets(from.getOutputDatasets());
        to.setProcessingDate(from.getProcessingDate());
        to.setStudyId(from.getStudyId());
    }

    /**
     * Create or update Shanoir event relative to an execution monitoring
     */
    private ShanoirEvent initShanoirEvent(ExecutionMonitoring processing, ShanoirEvent event, String execLabel) {
        String startMsg = execLabel + " : " + ExecutionStatus.RUNNING.getRestLabel();

        if (event == null) {
            event = new ShanoirEvent(
                    ShanoirEventType.EXECUTION_MONITORING_EVENT,
                    processing.getId().toString(),
                    KeycloakUtil.getTokenUserId(),
                    startMsg,
                    ShanoirEvent.IN_PROGRESS,
                    DEFAULT_PROGRESS);
        } else {
            event.setMessage(startMsg);
            event.setStatus(ShanoirEvent.IN_PROGRESS);
            event.setProgress(DEFAULT_PROGRESS);
        }
        eventService.publishEvent(event);
        return event;
    }

    /**
     * Manage execution monitoring with a non successfull status
     */
    private void processKilledJob(ExecutionMonitoring processing, ShanoirEvent event, VipExecutionDTO vipExecutionDTO) throws EntityNotFoundException {
        String execLabel = getExecLabel(processing);

        LOG.warn("{} status is [{}]", execLabel, vipExecutionDTO.getStatus().getRestLabel());

        processing.setStatus(vipExecutionDTO.getStatus());
        update(processing);

        LOG.info("Execution status updated, stopping job...");

        stop.set(true);

        setEventInError(event, execLabel + " : "  + vipExecutionDTO.getStatus().getRestLabel()
                + (vipExecutionDTO.getErrorCode() != null ? " (Error code : " + vipExecutionDTO.getErrorCode() + ")" : ""));
    }

    /**
     * Manage execution monitoring with a successfull status
     */
    private void processFinishedJob(ExecutionMonitoring execution, ShanoirEvent event, Long endDate) throws EntityNotFoundException, ResultHandlerException {

        String execLabel = getExecLabel(execution);
        execution.setStatus(ExecutionStatus.FINISHED);
        execution.setEndDate(endDate);
        execution.setProcessingDate(LocalDate.now());

        update(execution);

        LOG.info("{} status is [{}]", execLabel, ExecutionStatus.FINISHED.getRestLabel());
        event.setMessage(execLabel + " : Finished. Processing imported results...");
        eventService.publishEvent(event);

        outputService.process(execution);

        LOG.info("Execution status updated, stopping job...");

        stop.set(true);

        event.setMessage(execLabel + " : Finished");
        event.setStatus(ShanoirEvent.SUCCESS);
        event.setProgress(1f);
        eventService.publishEvent(event);
    }

    /**
     * Return the label of ane xecution monitoring
     */
    private String getExecLabel(ExecutionMonitoring processing) {
        return "VIP Execution [" + processing.getName() + "]";
    }

    /**
     * Set the shanoir execution monitoring event in error status
     */
    private void setEventInError(ShanoirEvent event, String msg) {
        event.setMessage(msg);
        event.setStatus(ShanoirEvent.ERROR);
        event.setProgress(1f);
        eventService.publishEvent(event);
    }
}
