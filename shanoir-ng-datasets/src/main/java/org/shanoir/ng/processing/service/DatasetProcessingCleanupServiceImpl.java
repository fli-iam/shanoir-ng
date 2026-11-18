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

package org.shanoir.ng.processing.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

import org.shanoir.ng.processing.dto.DatasetProcessingCleanupRecord;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository.CleanupDataset;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository.CleanupLink;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository.CleanupProcessing;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DatasetProcessingCleanupServiceImpl implements DatasetProcessingCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetProcessingCleanupServiceImpl.class);

    private static final int PROGRESS_STEP = 100;

    private static final BinaryOperator<CleanupProcessing> LATEST = BinaryOperator.maxBy(
            Comparator.<CleanupProcessing, Long>comparing(CleanupProcessing::getStartDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(CleanupProcessing::getId));

    private record Fingerprint(String comment, Set<Long> inputs) {
    }

    @Autowired
    private DatasetProcessingRepository repository;

    @Autowired
    private DatasetProcessingService processingService;

    @Autowired
    private ShanoirEventService eventService;

    @Override
    public DatasetProcessingCleanupRecord plan() {
        return computePlan(repository.findCleanupProcessings(), repository.findCleanupInputs(),
                repository.findCleanupResources(), repository.findCleanupDatasets());
    }

    @Override
    @Async
    public void clean(DatasetProcessingCleanupRecord plan) {
        List<Long> ids = plan.processingIds();
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DELETE_DATASET_PROCESSING_EVENT, "cleanup",
                KeycloakUtil.getTokenUserId(), "Cleaning " + ids.size() + " dataset processings...", ShanoirEvent.IN_PROGRESS, 0f);
        eventService.publishEvent(event);

        int done = 0;
        int failed = 0;
        for (Long id : ids) {
            try {
                // May already be gone: deleting a dataset also deletes the processings left without input
                if (repository.existsById(id)) {
                    processingService.deleteById(id, event);
                }
            } catch (Exception e) {
                failed++;
                LOG.error("Cleanup: dataset processing {} could not be deleted.", id, e);
            }
            done++;
            if (done % PROGRESS_STEP == 0) {
                eventService.publishEvent(event, "Cleaned " + done + "/" + ids.size() + " dataset processings...", (float) done / ids.size());
            }
        }

        if (failed > 0) {
            eventService.publishErrorEvent(event, failed + "/" + ids.size() + " dataset processings could not be cleaned, see the logs.");
        } else {
            eventService.publishSuccessEvent(event, ids.size() + " dataset processings cleaned.");
        }
    }

    static DatasetProcessingCleanupRecord computePlan(List<CleanupProcessing> processings, List<CleanupLink> inputs,
                                                      List<CleanupLink> resources, List<CleanupDataset> datasets) {
        Map<Long, Long> producerByDataset = new HashMap<>();
        Map<Long, List<Long>> outputsByProcessing = new HashMap<>();
        for (CleanupDataset dataset : datasets) {
            if (dataset.getProcessingId() != null) {
                producerByDataset.put(dataset.getId(), dataset.getProcessingId());
                outputsByProcessing.computeIfAbsent(dataset.getProcessingId(), k -> new ArrayList<>()).add(dataset.getId());
            }
        }

        Map<Long, Set<Long>> inputsByProcessing = new HashMap<>();
        Map<Long, List<Long>> usedByProcessing = new HashMap<>();
        for (CleanupLink input : inputs) {
            inputsByProcessing.computeIfAbsent(input.getProcessingId(), k -> new TreeSet<>()).add(input.getDatasetId());
            usedByProcessing.computeIfAbsent(input.getProcessingId(), k -> new ArrayList<>()).add(input.getDatasetId());
        }
        // Running executions have no input of their own: what they use is only known from their resources
        for (CleanupLink resource : resources) {
            usedByProcessing.computeIfAbsent(resource.getProcessingId(), k -> new ArrayList<>()).add(resource.getDatasetId());
        }

        Set<Long> toKeep = new HashSet<>();
        Map<Long, Long> parentByProcessing = new HashMap<>();
        Map<Fingerprint, CleanupProcessing> latest = new HashMap<>();
        for (CleanupProcessing processing : processings) {
            Long id = processing.getId();
            parentByProcessing.put(id, processing.getParentId());
            boolean hasOutputs = outputsByProcessing.containsKey(id);
            if (processing.getMonitoringId() == null) {
                // Not from an execution: no meaningful status, never compared to the others
                if (processing.getParentId() != null || hasOutputs) {
                    toKeep.add(id);
                }
            } else if (Objects.equals(processing.getMonitoringStatus(), ExecutionStatus.RUNNING.ordinal())) {
                toKeep.add(id);
            } else if (processing.getParentId() != null && hasOutputs
                    && Objects.equals(processing.getProcessingStatus(), ExecutionStatus.FINISHED.ordinal())) {
                Fingerprint fingerprint = new Fingerprint(processing.getComment(), inputsByProcessing.getOrDefault(id, Set.of()));
                latest.merge(fingerprint, processing, LATEST);
            }
        }
        latest.values().forEach(processing -> toKeep.add(processing.getId()));

        // DatasetService refuses to delete a dataset that has copies, and a copy relies on the files of its source
        for (CleanupDataset dataset : datasets) {
            Long sourceProducer = producerByDataset.get(dataset.getSourceId());
            if (sourceProducer != null) {
                toKeep.add(sourceProducer);
            }
        }

        // A retained processing retains its parent and the producers of the datasets it uses, recursively
        Deque<Long> pending = new ArrayDeque<>(toKeep);
        while (!pending.isEmpty()) {
            Long id = pending.poll();
            List<Long> required = new ArrayList<>();
            required.add(parentByProcessing.get(id));
            usedByProcessing.getOrDefault(id, List.of()).forEach(dataset -> required.add(producerByDataset.get(dataset)));
            for (Long requiredId : required) {
                if (requiredId != null && toKeep.add(requiredId)) {
                    pending.add(requiredId);
                }
            }
        }

        List<Long> children = new ArrayList<>();
        List<Long> roots = new ArrayList<>();
        List<Long> monitorings = new ArrayList<>();
        List<Long> outputs = new ArrayList<>();
        for (CleanupProcessing processing : processings) {
            Long id = processing.getId();
            if (!toKeep.contains(id)) {
                (processing.getParentId() != null ? children : roots).add(id);
                if (id.equals(processing.getMonitoringId())) {
                    monitorings.add(id);
                }
                outputs.addAll(outputsByProcessing.getOrDefault(id, List.of()));
            }
        }
        children.addAll(roots);
        return new DatasetProcessingCleanupRecord(children, monitorings, outputs);
    }
}
