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

package org.shanoir.ng.vip.executionTemplate.service;

import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.shared.service.TransactionRunner;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.service.ExecutionService;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionInQueue;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.repository.PlannedExecutionRepository;
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
import java.util.stream.StreamSupport;

@Service
public class PlannedExecutionManager {

    private static final Logger LOG = LoggerFactory.getLogger(PlannedExecutionManager.class);

    private  int maxStatusRetries = 3;
    private long statusSleepSeconds = 20;
    private int maxThreads = 3;

    private List<ExecutionInQueue> executionsQueue = new ArrayList<>();
    private List<Long> involvedDatasetIds = new ArrayList<>();

    private static ExecutorService executor;
    //Manage executor shutdown at app termination
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(PlannedExecutionManager::shutdownExecutor));
    }

    @Value("${server.shanoir-hours.shutdown}")
    private int shutdownHour;

    @Value("${server.shanoir-hours.continuance}")
    private int continuanceHour;

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

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    public PlannedExecutionManager() {
        executor = Executors.newFixedThreadPool(maxThreads);
    }

    protected synchronized void manageExecutionsQueue() {
        if (running) return;
        running = true;

        try {
            while (!executionsQueue.isEmpty()) {

                // Sort by priority ascending
                executionsQueue.sort(Comparator.comparingInt(exec -> exec.getTemplate().getPriority()));

                // Find next runnable execution
                ExecutionInQueue next = null;
                for (ExecutionInQueue candidate : executionsQueue) {
                    List<Long> candidateData = plannedExecutionServiceImpl.getInvolvedData(candidate);
                    if (Collections.disjoint(candidateData, involvedDatasetIds)) {
                        next = candidate;
                        //Lock the datasets already involved in an execution to ensure that the current execution is ended before starting the next one
                        // (ex: it would be an issue if the next execution use the current execution output as input)
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

                    // Submit execution.
                    // NB: threadExecution manages its own (short) transaction internally; the VIP status
                    // polling must run OUTSIDE any transaction so the processing resources are committed and
                    // visible to VIP's /carmin-data/path download callback while the execution is RUNNING.
                    executor.submit(() -> {
                        threadExecution(execution.getTemplate(), execution.getObjectId(), execution.getType(), execution.getPlannedExecutionToRemove());
                        involvedDatasetIds.removeAll(candidateData);
                    });
                } else {
                    try {
                        Thread.sleep(statusSleepSeconds);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            running = false;
            LOG.info("Auto executions for newly imported DICOM ended.");
        }
    }

    public synchronized void addToExecutionsQueue(ExecutionInQueue executionInQueue) {
        executionsQueue.add(executionInQueue);
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
        String offlineToken = template.getOfflineToken();
        if (offlineToken == null) {
            LOG.error("No offline token stored for template {}. Cannot execute without user credentials.", template.getId());
            return;
        }

        try {
            AccessTokenResponse tokenResponse = keycloakServiceAccountUtils.refreshUserToken(offlineToken);
            Map<String, Object> claims = SecurityContextUtil.decodeJwtClaims(tokenResponse.getToken());
            String username = (String) claims.getOrDefault("preferred_username", "shanoir");
            Object userIdRaw = claims.get("userId");
            Long userId = userIdRaw != null ? Long.valueOf(userIdRaw.toString()) : 92233720L;
            SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN", username, userId, tokenResponse.getToken());
        } catch (SecurityException e) {
            LOG.error("Failed to refresh user token for template {}. Execution aborted.", template.getId(), e);
            return;
        }

        // Create the monitoring, processing resources and submit to VIP in a SHORT transaction that commits
        // before VIP starts downloading. The processing resources (resourceId -> datasets mapping) must be
        // committed and visible to VIP's /carmin-data/path callback, otherwise the download fails with HTTP 400.
        final String[] vipIdentifierHolder = new String[1];
        try {
            transactionRunner.runInTransaction(em -> {
                try {
                    ExecutionCandidateDTO candidate = plannedExecutionServiceImpl.prepareExecutionCandidate(template, executionLevel, objectId);
                    if (Objects.nonNull(candidate)) {
                        candidate.setRefreshToken(offlineToken);
                        IdName monitoringIdName = executionService.createExecutions(List.of(candidate));
                        vipIdentifierHolder[0] = executionMonitoringService.getVipIdentifierFromMonitoringId(monitoringIdName.getId());
                        for (Long acquisitionId : plannedExecutionToRemoveWithAcquisitionId) {
                            plannedExecutionRepository.deleteByAcquisitionIdAndTemplateId(acquisitionId, template.getId());
                        }
                    }
                } catch (RestServiceException | SecurityException | EntityNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            LOG.error("Execution from template {} for {} {} failed.", template.getId(), executionLevel, objectId, cause);
            return;
        }

        // Transaction committed: processing resources are now visible to VIP. Poll status OUTSIDE the transaction
        // so the dataset lock (involvedDatasetIds) is held until the execution finishes, serializing dependent runs.
        String vipIdentifier = vipIdentifierHolder[0];
        if (vipIdentifier == null) {
            return;
        }

        try {
            ExecutionStatus status = ExecutionStatus.RUNNING;
            while (Objects.equals(status, ExecutionStatus.RUNNING)) {
                TimeUnit.SECONDS.sleep(statusSleepSeconds);

                for (int attempt = 1; attempt <= maxStatusRetries; attempt++) {
                    try {
                        status = executionService.getExecutionStatusFromVipIdentifier(vipIdentifier);
                        break;
                    } catch (Exception e) {
                        if (attempt == maxStatusRetries) {
                            break;
                        }
                        TimeUnit.SECONDS.sleep(1);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Status polling interrupted for execution from template {} ({} {}).", template.getId(), executionLevel, objectId, e);
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
        LocalTime start = LocalTime.of(shutdownHour, 0);
        LocalTime end = LocalTime.of(continuanceHour, 0);

        if (!now.isBefore(start) && now.isBefore(end)) {
            Duration pause = Duration.between(now, end);
            LOG.info("Execution paused until {} AM.", continuanceHour);
            try {
                Thread.sleep(pause.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
