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
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Exceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author KhalilKes
 */
@Service
@EnableAsync
public class ExecutionMonitoringServiceImpl implements ExecutionMonitoringService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final DateTimeFormatter readableFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
    public static final float DEFAULT_PROGRESS = 0.5f;
    @Value("${vip.sleep-time}")
    private long sleepTime;
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionMonitoringServiceImpl.class);
    private static final String RIGHT_STR = "CAN_SEE_ALL";
    private final ConcurrentLinkedQueue<Map<String, Object>> monitoringQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isRunning = false;

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

    @Autowired
    @Lazy
    private ExecutionMonitoringServiceImpl emProxyService;

    public ExecutionMonitoring createExecutionMonitoring(ExecutionCandidateDTO sample) throws RestServiceException {
        ExecutionMonitoring executionMonitoring = new ExecutionMonitoring();

        executionMonitoring.setName(sample.getPipelineIdentifier().replaceAll("[/.]", "_") + "_" + LocalDateTime.now().format(readableFormatter));
        executionMonitoring.setPipelineIdentifier(sample.getPipelineIdentifier());
        executionMonitoring.setResultsLocation(KeycloakUtil.getTokenUserId() + "/" + formatter.format(LocalDateTime.now()));
        executionMonitoring.setTimeout(20);
        executionMonitoring.setStudyId(sample.getStudyIdentifier());
        executionMonitoring.setStatus(ExecutionStatus.RUNNING);
        executionMonitoring.setComment(executionMonitoring.getName());
        executionMonitoring.setDatasetProcessingType(DatasetProcessingType.valueOf(sample.getProcessingType()));
        executionMonitoring.setOutputProcessing(null);
        executionMonitoring.setInputDatasets(null);
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


    public void startMonitoringJob(ExecutionMonitoring createdMonitoring, ShanoirEvent event, Integer jobsNumber) {
        Map<String, Object> monitoringMap = new HashMap<>();
        monitoringMap.put("monitoring", createdMonitoring);
        monitoringMap.put("event", event);
        monitoringMap.put("attempt", 1);
        monitoringMap.put("jobsNumber", jobsNumber);
        monitoringQueue.add(monitoringMap);

        if (!isRunning) { //If we remove this line, each calling thread needs to wait the old ones to finish the synchronized block below before resuming the code execution. It's only for code performance.
            synchronized (this) { //Allow the synchronized block to be executed only by one thread at a time. It avoids concurrency
                if (!isRunning) {  //In case of two calling threads hitting the 1st !isRunning check condition at the same time, it may leads to 2 distinct monitoring loop if we remove this second !isRunning check, what we want to avoid
                    isRunning = true;
                    emProxyService.monitoringLoop();
                }
            }
        }
    }


    @Async //We keep that method async, because the startMonitoringJob ensure that only one thread of this method can exist at a time, and it doesn't block the 1st calling method
    protected void monitoringLoop() {
        while (!monitoringQueue.isEmpty()) {
            long startTime = System.currentTimeMillis();

            for (Map<String, Object> emMap : monitoringQueue) {
                ExecutionMonitoring monitoring = (ExecutionMonitoring) emMap.get("monitoring");
                ShanoirEvent event = (ShanoirEvent) emMap.get("event");
                Integer attempt = (Integer) emMap.get("attempt");
                Integer jobsNumber = (Integer) emMap.get("jobsNumber");
                String execLabel = getExecLabel(monitoring);


                if (Objects.isNull(event) || !Objects.equals(event.getStatus(), ShanoirEvent.IN_PROGRESS)) {
                    event = initShanoirEvent(monitoring, event, execLabel, jobsNumber);
                    emMap.put("event", event);
                    LOG.info("Monitoring of execution id: " + monitoring.getId() + ", identifier: " + monitoring.getPipelineIdentifier() + ", name: " + monitoring.getName() + " started");
                }

                try {
                    VipExecutionDTO dto = executionService.getExecutionAsServiceAccount(attempt, monitoring.getIdentifier()).block();
                    if (dto == null) {
                        emMap.put("attempt", (Integer) emMap.get("attempt") + 1);
                        continue;
                    } else {
                        emMap.put("attempt", 1);
                    }

                    switch (dto.getStatus()) {
                        case FINISHED -> {
                            monitoring.setJobs(dto.getJobs());
                            monitoring.setStatus(dto.getStatus());
                            emProxyService.processFinishedJob(monitoring, event, dto.getEndDate());
                        }
                        case UNKNOWN, EXECUTION_FAILED, KILLED -> {
                            monitoring.setJobs(dto.getJobs());
                            monitoring.setStatus(dto.getStatus());
                            emProxyService.processKilledJob(monitoring, event, dto);
                        }
                        default -> {
                            if (!(Objects.isNull(dto.getJobs()) || Objects.equals(dto.getJobs().size(), 0))) {
                                Integer doneJobs = dto.getJobs().values().stream().mapToInt(e -> (ExecutionStatus.RUNNING.getRestLabel().toUpperCase().equals(e.get("status")) || ExecutionStatus.QUEUED.getRestLabel().toUpperCase().equals(e.get("status"))) ? 0 : 1).sum();
                                updateShanoirEvent(event, doneJobs, jobsNumber, execLabel);
                            }
                        }
                    }
                    if (!Objects.equals(dto.getStatus(), ExecutionStatus.RUNNING)) {
                        monitoringQueue.remove(emMap);
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    // Unwrap ReactiveException thrown from async method
                    Throwable ex = Exceptions.unwrap(e);
                    LOG.error("Error while monitoring processing {}. Stopping the monitoring ...", monitoring.getId(), ex.getCause());
                    if (Objects.nonNull(event)) {
                        setEventInError(event, execLabel + " : " + ex.getMessage());
                    }
                    monitoringQueue.remove(emMap);
                }
            }
            while (System.currentTimeMillis() - startTime < sleepTime) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.error("Error in the monitoring loop", e);
                }
            }
        }
        isRunning = false;
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
    private ShanoirEvent initShanoirEvent(ExecutionMonitoring processing, ShanoirEvent event, String execLabel, Integer jobsNumber) {
        String startMsg = execLabel + " : " + ExecutionStatus.RUNNING.getRestLabel() + " (0/" + jobsNumber + " jobs done)";

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
     * Update Shanoir event relative to an execution monitoring
     */
    private ShanoirEvent updateShanoirEvent(ShanoirEvent event, Integer doneJobs, Integer jobsNumber, String execLabel) {
        event.setMessage(execLabel + " : " + ExecutionStatus.RUNNING.getRestLabel() + " (" + doneJobs + "/" + jobsNumber + " jobs done)");
        eventService.publishEvent(event);
        return event;
    }

    /**
     * Manage execution monitoring with a non successfull status
     */
    @Transactional
    protected void processKilledJob(ExecutionMonitoring execution, ShanoirEvent event, VipExecutionDTO vipExecutionDTO) throws EntityNotFoundException {
        String execLabel = getExecLabel(execution);

        LOG.info("Execution id: {}, identifier: {}, name: {} status is [{}]", execution.getId(), execution.getPipelineIdentifier(), execution.getName(), vipExecutionDTO.getStatus().getRestLabel());
        execution.setStatus(vipExecutionDTO.getStatus());
        update(execution);

        LOG.info("Execution status updated, stopping job...");

        setEventInError(event, execLabel + " : "  + vipExecutionDTO.getStatus().getRestLabel()
                + (vipExecutionDTO.getErrorCode() != null ? " (Error code : " + vipExecutionDTO.getErrorCode() + ")" : ""));
    }

    /**
     * Manage execution monitoring with a successfull status
     */
    @Transactional
    protected void processFinishedJob(ExecutionMonitoring execution, ShanoirEvent event, Long endDate) throws EntityNotFoundException, ResultHandlerException {

        String execLabel = getExecLabel(execution);
        execution.setStatus(ExecutionStatus.FINISHED);
        execution.setEndDate(endDate);
        execution.setProcessingDate(LocalDate.now());

        update(execution);

        LOG.info("Execution id: {}, identifier: {}, name: {} status is [{}]", execution.getId(), execution.getPipelineIdentifier(), execution.getName(), ExecutionStatus.FINISHED.getRestLabel());        event.setMessage(execLabel + " : Finished. Processing imported results...");
        eventService.publishEvent(event);

        outputService.process(execution);

        LOG.info("Execution status updated, stopping job...");

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
