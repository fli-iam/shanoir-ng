package org.shanoir.ng.vip.planning.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.vip.planning.model.PlannedExecution;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface PlannedExecutionService {

    @Async
    void checkForPlannedExecutions(List<DatasetAcquisition> createdAcquisitions);

    List<PlannedExecution> findByStudyId(Long studyId);

    PlannedExecution update(PlannedExecution plannedExecution);

    PlannedExecution findById(Long executionId);

    void delete(Long executionId);

    PlannedExecution save(PlannedExecution plannedExecution);
}
