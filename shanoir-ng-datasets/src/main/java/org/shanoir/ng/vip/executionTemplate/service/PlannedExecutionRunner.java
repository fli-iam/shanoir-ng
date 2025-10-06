package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.service.TransactionRunner;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.service.ExecutionService;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionInQueue;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.repository.PlannedExecutionRepository;
import org.shanoir.ng.vip.shared.dto.DatasetParameterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PlannedExecutionRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PlannedExecutionRunner.class);

    private int MAX_STATUS_RETRIES = 3;
    private long STATUS_SLEEP_SECONDS = 20;
    private int MAX_THREADS = 3;

    private List<ExecutionInQueue> executionsQueue = new ArrayList<>();
    private List<Long> involvedDatasetIds = new ArrayList<>();

    private static ExecutorService executor;
    //Manage executor shutdown at app termination
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(PlannedExecutionRunner::shutdownExecutor));
    }

    @Value("${server.shanoir-hours.shutdown}")
    private int SHUTDOWN_HOUR;

    @Value("${server.shanoir-hours.continuance}")
    private int CONTINUANCE_HOUR;

    private boolean running = false;

    @Autowired
    private PlannedExecutionService plannedExecutionServiceImpl;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private ExecutionMonitoringService executionMonitoringService;

    @Autowired
    private PlannedExecutionRepository plannedExecutionRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private TransactionRunner transactionRunner;

    public PlannedExecutionRunner() {
        executor = Executors.newFixedThreadPool(MAX_THREADS);
    }

    protected synchronized void manageExecutionsQueue() {
        if (running) return;
        running = true;
        LOG.error("Token Loop");


        try {
            while (!executionsQueue.isEmpty()) {

                LOG.error("Token Queue");

                // Sort by priority ascending
                executionsQueue.sort(Comparator.comparingInt(exec -> exec.getTemplate().getPriority()));

                // Find next runnable execution
                ExecutionInQueue next = null;
                for (ExecutionInQueue candidate : executionsQueue) {
                    LOG.error("Token Involvement");
                    List<Long> candidateData = plannedExecutionServiceImpl.getInvolvedData(candidate);
                    if (Collections.disjoint(candidateData, involvedDatasetIds)) {
                        next = candidate;
                        //Lock the datasets already involved in an execution to ensure that the current execution is ended before starting the next one
                        // (ex: it would be an issue if the next issue use the current execution output as input)
                        addToInvolvedDatasetIds(candidateData);
                        break;
                    }
                }

                if (next != null) {
                    // Set involved datasets for this execution
                    List<Long> candidateData = plannedExecutionServiceImpl.getInvolvedData(next);
                    involvedDatasetIds.addAll(candidateData);
                    executionsQueue.remove(next);

                    //Execution in executor submission has to be final
                    ExecutionInQueue execution = next;

                    LOG.error("Token Submission");

                    // Submit execution
                    executor.submit(() -> {
                        transactionRunner.runInTransaction(em ->
                                threadExecution(execution.getTemplate(), execution.getObjectId(), execution.getType(), execution.getPlannedExecutionToRemove()));
                        involvedDatasetIds.removeAll(candidateData);
                    });
                } else {
                    try {
                        Thread.sleep(STATUS_SLEEP_SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            LOG.error("Token Thread Stop");

            running = false;
            LOG.info("Auto executions for newly imported DICOM ended.");
        }
    }

    public synchronized void addToExecutionsQueue(ExecutionInQueue executionInQueue) {
        executionsQueue.add(executionInQueue);
        LOG.error("Token 5");

        manageExecutionsQueue();
    }

    public synchronized void addToInvolvedDatasetIds(List<Long> longs) {
        for (Long acquisitionId : longs) {
            involvedDatasetIds.addAll(StreamSupport.stream(datasetRepository.findByDatasetAcquisitionId(acquisitionId).spliterator(), false).map(Dataset::getId).toList());
        }
    }
    
    /**
     * Thread the creation of monitoring and start of execution for the given template id and acquisition id
     */
    private void threadExecution(ExecutionTemplate template, Long objectId, String executionLevel, List<Long> plannedExecutionToRemoveWithAcquisitionId) {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");

        LOG.error("Token Thread start");

        try {
            ExecutionCandidateDTO candidate = plannedExecutionServiceImpl.prepareExecutionCandidate(template, executionLevel, objectId);
            LOG.error("Token candidate" + candidate.toString());

            if(Objects.nonNull(candidate)) {
                IdName monitoringIdName = executionService.createExecution(candidate, datasetRepository.findByIdIn(candidate.getDatasetParameters().stream().map(DatasetParameterDTO::getDatasetIds).flatMap(List::stream).collect(Collectors.toList())));
                LOG.error("Token idName" + monitoringIdName.getName() + monitoringIdName.getId());

                String vipIdentifier = executionMonitoringService.getVipIdentifierFromMonitoringId(monitoringIdName.getId());


                LOG.error("Token VipIdentifier" + vipIdentifier);
                ExecutionStatus status = ExecutionStatus.RUNNING;

                while (Objects.equals(status, ExecutionStatus.RUNNING)) {
                    TimeUnit.SECONDS.sleep(STATUS_SLEEP_SECONDS);

                    for (int attempt = 1; attempt <= MAX_STATUS_RETRIES; attempt++) {
                        try {
                            status = executionService.getExecutionStatusFromVipIdentifier(vipIdentifier);
                            break;
                        } catch (Exception e) {
                            if (attempt == MAX_STATUS_RETRIES){
                                break;
                            }
                            TimeUnit.SECONDS.sleep(1);
                        }
                    }

                    for (Long acquisitionId : plannedExecutionToRemoveWithAcquisitionId) {
                        plannedExecutionRepository.deleteByAcquisitionIdAndTemplateId(acquisitionId, template.getId());
                    }
                }
            }
        } catch (RestServiceException | SecurityException | EntityNotFoundException | InterruptedException e) {
            LOG.error("Execution from template {} for {} {} failed.", template.getId(), executionLevel, objectId, e);
        }
    }

    private static void shutdownExecutor() {
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Stop the threading for avoiding side effect at instance shutdown
     */
    private void pauseIfInSleepWindow() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(SHUTDOWN_HOUR, 0);
        LocalTime end = LocalTime.of(CONTINUANCE_HOUR, 0);

        if (!now.isBefore(start) && now.isBefore(end)) {
            Duration pause = Duration.between(now, end);
            LOG.info("Execution paused until {} AM.", CONTINUANCE_HOUR);
            try {
                Thread.sleep(pause.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
