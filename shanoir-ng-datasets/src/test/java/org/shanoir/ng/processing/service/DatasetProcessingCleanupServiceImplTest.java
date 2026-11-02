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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository.CleanupDataset;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository.CleanupLink;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository.CleanupProcessing;

class DatasetProcessingCleanupServiceImplTest {

    private static final int RUNNING = 0;
    private static final int FINISHED = 1;
    private static final int FAILED = 2;

    private record Processing(Long getId, Long getParentId, String getComment, Integer getProcessingStatus,
            Long getMonitoringId, Integer getMonitoringStatus, Long getStartDate) implements CleanupProcessing {
    }

    private record Link(Long getProcessingId, Long getDatasetId) implements CleanupLink {
    }

    private record Output(Long getId, Long getProcessingId, Long getSourceId) implements CleanupDataset {
    }

    private static Processing monitoring(long id, int status) {
        return new Processing(id, null, "name" + id, 3, id, status, id);
    }

    private static Processing child(long id, long monitoringId, int status, long startDate) {
        return new Processing(id, monitoringId, "pipeline", status, monitoringId, FINISHED, startDate);
    }

    private static List<Long> toDelete(List<CleanupProcessing> processings, List<CleanupLink> inputs, List<CleanupLink> resources, List<CleanupDataset> datasets) {
        return DatasetProcessingCleanupServiceImpl.computePlan(processings, inputs, resources, datasets).processingIds();
    }

    @Test
    void keepsOnlyTheLatestForSameCommentAndInputs() {
        List<CleanupProcessing> processings = List.of(monitoring(1, FINISHED), child(10, 1, FINISHED, 100), monitoring(2, FINISHED), child(20, 2, FINISHED, 200));
        List<CleanupLink> inputs = List.of(new Link(10L, 500L), new Link(20L, 500L));
        List<CleanupDataset> datasets = List.of(new Output(600L, 10L, null), new Output(700L, 20L, null));

        assertEquals(List.of(10L, 1L), toDelete(processings, inputs, List.of(), datasets));
    }

    @Test
    void differentInputsAreNotDuplicates() {
        List<CleanupProcessing> processings = List.of(monitoring(1, FINISHED), child(10, 1, FINISHED, 100), child(11, 1, FINISHED, 200));
        List<CleanupLink> inputs = List.of(new Link(10L, 500L), new Link(11L, 501L));
        List<CleanupDataset> datasets = List.of(new Output(600L, 10L, null), new Output(601L, 11L, null));

        assertEquals(List.of(), toDelete(processings, inputs, List.of(), datasets));
    }

    @Test
    void latestWithoutOutputsDoesNotReplaceAnOlderOneWithOutputs() {
        List<CleanupProcessing> processings = List.of(monitoring(1, FINISHED), child(10, 1, FINISHED, 100), monitoring(2, FINISHED), child(20, 2, FINISHED, 200));
        List<CleanupLink> inputs = List.of(new Link(10L, 500L), new Link(20L, 500L));
        List<CleanupDataset> datasets = List.of(new Output(600L, 10L, null));

        assertEquals(List.of(20L, 2L), toDelete(processings, inputs, List.of(), datasets));
    }

    @Test
    void removesNotFinishedProcessingsAndEmptyMonitorings() {
        List<CleanupProcessing> processings = List.of(monitoring(1, FAILED), monitoring(2, FINISHED), child(20, 2, FAILED, 100));
        List<CleanupDataset> datasets = List.of(new Output(600L, 20L, null));

        assertEquals(List.of(20L, 1L, 2L), toDelete(processings, List.of(), List.of(), datasets));
    }

    @Test
    void keepsRunningExecutionsAndWhatTheyUse() {
        List<CleanupProcessing> processings = List.of(monitoring(1, RUNNING), monitoring(2, FAILED), child(20, 2, FAILED, 100));
        List<CleanupLink> resources = List.of(new Link(1L, 600L));
        List<CleanupDataset> datasets = List.of(new Output(600L, 20L, null));

        assertEquals(List.of(), toDelete(processings, List.of(), resources, datasets));
    }

    @Test
    void keepsTheChainBehindAnOutputUsedAsInput() {
        List<CleanupProcessing> processings = List.of(
                monitoring(1, FINISHED), child(10, 1, FINISHED, 100),
                monitoring(2, FINISHED), child(20, 2, FINISHED, 200),
                monitoring(3, FINISHED), child(30, 3, FINISHED, 300));
        List<CleanupLink> inputs = List.of(new Link(10L, 500L), new Link(20L, 500L), new Link(30L, 600L));
        List<CleanupDataset> datasets = List.of(new Output(600L, 10L, null), new Output(700L, 20L, null), new Output(800L, 30L, null));

        assertEquals(List.of(), toDelete(processings, inputs, List.of(), datasets));
    }

    @Test
    void doesNotKeepAnOutputOnlyUsedByADeletedProcessing() {
        List<CleanupProcessing> processings = List.of(
                monitoring(1, FINISHED), child(10, 1, FINISHED, 100),
                monitoring(2, FINISHED), child(20, 2, FINISHED, 200),
                monitoring(3, FAILED), child(30, 3, FAILED, 300));
        List<CleanupLink> inputs = List.of(new Link(10L, 500L), new Link(20L, 500L), new Link(30L, 600L));
        List<CleanupDataset> datasets = List.of(new Output(600L, 10L, null), new Output(700L, 20L, null));

        assertEquals(List.of(10L, 30L, 1L, 3L), toDelete(processings, inputs, List.of(), datasets));
    }

    @Test
    void keepsTheProducerOfACopiedDataset() {
        List<CleanupProcessing> processings = List.of(monitoring(1, FAILED), child(10, 1, FAILED, 100));
        List<CleanupDataset> datasets = List.of(new Output(600L, 10L, null), new Output(601L, null, 600L));

        assertEquals(List.of(), toDelete(processings, List.of(), List.of(), datasets));
    }

    @Test
    void neverTouchesProcessingsWithoutExecutionThatHaveOutputs() {
        List<CleanupProcessing> processings = List.of(new Processing(1L, null, "manual", 0, null, null, null));
        List<CleanupDataset> datasets = List.of(new Output(600L, 1L, null));

        assertEquals(List.of(), toDelete(processings, List.of(), List.of(), datasets));
    }
}
