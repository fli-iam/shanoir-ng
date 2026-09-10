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
import org.shanoir.ng.utils.SecurityContextUtil;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
    /** Pause observed after each polled execution, so a full queue does not hammer the VIP API. In ms. */
    private long jobPollDelay = 10000;
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionMonitoringServiceImpl.class);
    private static final String RIGHT_STR = "CAN_SEE_ALL";
    private final ConcurrentLinkedQueue<MonitoringJob> monitoringQueue = new ConcurrentLinkedQueue<>();
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

    public ExecutionMonitoring createExecutionMonitoring(ExecutionCandidateDTO sample) throws RestServiceException, EntityNotFoundException {
        ExecutionMonitoring executionMonitoring = new ExecutionMonitoring();

        executionMonitoring.setName(sample.getPipelineIdentifier().replaceAll("[/.]", "_") + "_" + LocalDateTime.now().format(readableFormatter));
        executionMonitoring.setPipelineIdentifier(sample.getPipelineIdentifier());
        executionMonitoring.setResultsLocation(KeycloakUtil.getTokenUserId() + "/" + formatter.format(LocalDateTime.now()));
        executionMonitoring.setTimeout(20);
        executionMonitoring.setStudyId(sample.getStudyIdentifier());
        executionMonitoring.setStatus(ExecutionStatus.RUNNING);
        executionMonitoring.setProcessingStatus(ExecutionStatus.UNKNOWN);
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
        if (event == null) {
            // Report the execution to its owner right now, on their own thread. Creating the event on the first
            // poll instead would delay it by one poll round, so a user queuing an execution while others are
            // already being monitored would not see their task appear for several seconds. It also guarantees the
            // event is addressed to the user who queued the execution rather than to whoever runs the loop.
            event = createShanoirEvent(createdMonitoring, jobsNumber);
        }
        MonitoringJob job = new MonitoringJob(createdMonitoring, event, jobsNumber);
        LOG.info("Monitoring of execution id: " + createdMonitoring.getId() + ", identifier: " + createdMonitoring.getPipelineIdentifier() + ", name: " + createdMonitoring.getName() + " started");

        // Queuing the job and starting a loop for it must be atomic with respect to the loop's own exit check, see
        // hasJobsToPoll(). The guarded section is a queue add, a boolean read and an async submit that returns at
        // once, never a monitoring round, so concurrent callers are not made to wait on each other in practice.
        synchronized (this) {
            monitoringQueue.add(job);
            if (!isRunning) {
                isRunning = true;
                emProxyService.monitoringLoop();
            }
        }
    }


    @Async // We keep that method async, because the startMonitoringJob ensure that only one thread of this method can exist at a time, and it doesn't block the 1st calling method
    protected void monitoringLoop() {
        try {
            while (hasJobsToPoll()) {
                long startTime = System.currentTimeMillis();

                for (MonitoringJob job : monitoringQueue) {
                    processMonitoringJob(job);
                }
                while (System.currentTimeMillis() - startTime < sleepTime) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.error("Error in the monitoring loop", e);
                    }
                }
            }
        } finally {
            releaseLoop();
        }
    }

    /**
     * Tell whether the monitoring loop has another round to run, and release it if the queue has been drained.
     *
     * Reading the queue and clearing the running flag has to be atomic with respect to {@link #startMonitoringJob}:
     * otherwise an execution queued in between is left in the queue while the loop stops believing it is empty, and
     * nothing ever polls it again. Its owner then never hears about their execution, and its results are never
     * imported (issue #3813).
     */
    private synchronized boolean hasJobsToPoll() {
        if (monitoringQueue.isEmpty()) {
            isRunning = false;
            return false;
        }
        return true;
    }

    /**
     * Release the monitoring loop, so that the next queued execution starts a new one. Called on every exit path,
     * as an unexpected error leaving the flag set would silently stop all VIP monitoring until the next restart.
     */
    private synchronized void releaseLoop() {
        isRunning = false;
    }

    /**
     * Poll one queued execution under its own, self-contained security context.
     *
     * The monitoring loop is a background system task shared by every user: it is started by whoever happened to
     * queue the first execution, and the async executor pins that user's security context to the loop thread for
     * its whole lifetime. Reading the connected user from the thread here therefore yields an arbitrary user, which
     * is what made executions be reported to the wrong person (issue #3813). It also made result import depend on
     * that user's rights.
     *
     * So each job is polled under a system context instead, exactly as the resumption runner already does on
     * startup, and the event addressing the execution to its owner is built upfront by {@link #startMonitoringJob}
     * on the owner's own thread. The previous context is restored afterwards so nothing leaks from one job to the
     * next.
     */
    private void processMonitoringJob(MonitoringJob job) {
        SecurityContext previousContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        SecurityContextUtil.initAuthenticationContext(KeycloakUtil.ROLE_ADMIN);
        try {
            pollMonitoringJob(job);
        } finally {
            SecurityContextHolder.setContext(previousContext);
        }
    }

    /**
     * Poll VIP for the state of a single queued execution and handle its outcome. The job is dropped from the
     * monitoring queue as soon as its execution is no longer running, or if the polling itself failed.
     */
    private void pollMonitoringJob(MonitoringJob job) {
        ExecutionMonitoring monitoring = job.monitoring;
        String execLabel = getExecLabel(monitoring);

        if (!Objects.equals(job.event.getStatus(), ShanoirEvent.IN_PROGRESS)) {
            resumeShanoirEvent(job.event, monitoring, job.jobsNumber);
            LOG.info("Monitoring of execution id: " + monitoring.getId() + ", identifier: " + monitoring.getPipelineIdentifier() + ", name: " + monitoring.getName() + " resumed");
        }

        try {
            VipExecutionDTO dto = executionService.getExecutionAsServiceAccount(job.attempt, monitoring.getIdentifier()).block();
            if (dto == null) {
                job.attempt++;
                return;
            } else {
                job.attempt = 1;
            }

            switch (dto.getStatus()) {
                case FINISHED -> {
                    monitoring.setJobs(dto.getJobs());
                    monitoring.setStatus(dto.getStatus());
                    LOG.info("Monitoring of execution id: " + monitoring.getId() + ", status: " + monitoring.getStatus());
                    emProxyService.processFinishedJob(monitoring, job.event, dto.getEndDate());
                }
                case UNKNOWN, EXECUTION_FAILED, KILLED -> {
                    monitoring.setJobs(dto.getJobs());
                    monitoring.setStatus(dto.getStatus());
                    LOG.info("Monitoring of execution id: " + monitoring.getId() + ", status: " + monitoring.getStatus());
                    emProxyService.processKilledJob(monitoring, job.event, dto);
                }
                default -> {
                    if (!(Objects.isNull(dto.getJobs()) || Objects.equals(dto.getJobs().size(), 0))) {
                        Integer doneJobs = dto.getJobs().values().stream().mapToInt(e -> (ExecutionStatus.RUNNING.getRestLabel().toUpperCase().equals(e.get("status")) || ExecutionStatus.QUEUED.getRestLabel().toUpperCase().equals(e.get("status"))) ? 0 : 1).sum();
                        updateShanoirEvent(job.event, doneJobs, job.jobsNumber, execLabel);
                    }
                }
            }
            if (!Objects.equals(dto.getStatus(), ExecutionStatus.RUNNING)) {
                monitoringQueue.remove(job);
            }
            Thread.sleep(jobPollDelay);
        } catch (Exception e) {
            // Unwrap ReactiveException thrown from async method
            Throwable ex = Exceptions.unwrap(e);
            LOG.error("Error while monitoring processing {}. Stopping the monitoring ...", monitoring.getId(), ex.getCause());
            if (Objects.nonNull(job.event)) {
                setEventInError(job.event, execLabel + " : " + ex.getMessage());
            }
            monitoringQueue.remove(job);
        }
    }

    public String getVipIdentifierFromMonitoringId(Long id) {
        return repository.findById(id).get().getIdentifier();
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
     * Create and publish the Shanoir event reporting an execution monitoring to its owner.
     *
     * Called on the thread that queues the execution, so the event is addressed to the user who launched it. The
     * monitoring loop must never create it: it is a background task running under a system context, and it would
     * only get to this execution once it is done polling the ones already queued.
     */
    private ShanoirEvent createShanoirEvent(ExecutionMonitoring processing, Integer jobsNumber) {
        ShanoirEvent event = new ShanoirEvent(
                ShanoirEventType.EXECUTION_MONITORING_EVENT,
                processing.getId().toString(),
                KeycloakUtil.getTokenUserId(),
                startMessage(processing, jobsNumber),
                ShanoirEvent.IN_PROGRESS,
                DEFAULT_PROGRESS);
        eventService.publishEvent(event);
        return event;
    }

    /**
     * Put back in progress the event of an execution whose monitoring is resumed, typically after a restart. The
     * event already belongs to the user who launched the execution, so its owner is left untouched.
     */
    private void resumeShanoirEvent(ShanoirEvent event, ExecutionMonitoring processing, Integer jobsNumber) {
        event.setMessage(startMessage(processing, jobsNumber));
        event.setStatus(ShanoirEvent.IN_PROGRESS);
        event.setProgress(DEFAULT_PROGRESS);
        eventService.publishEvent(event);
    }

    /**
     * Message reporting an execution monitoring that has yet to complete a single job
     */
    private String startMessage(ExecutionMonitoring processing, Integer jobsNumber) {
        return getExecLabel(processing) + " : " + ExecutionStatus.RUNNING.getRestLabel() + " (0/" + jobsNumber + " jobs done)";
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

    /**
     * A VIP execution awaiting monitoring, as held in {@link #monitoringQueue}.
     */
    private static final class MonitoringJob {

        private final ExecutionMonitoring monitoring;

        private final Integer jobsNumber;

        /** Shanoir event reporting this execution to its owner. Never null: built when the job is queued. */
        private ShanoirEvent event;

        /** Consecutive failed attempts to read this execution from VIP, reset as soon as one succeeds. */
        private int attempt = 1;

        MonitoringJob(ExecutionMonitoring monitoring, ShanoirEvent event, Integer jobsNumber) {
            this.monitoring = monitoring;
            this.event = event;
            this.jobsNumber = jobsNumber;
        }
    }
}
