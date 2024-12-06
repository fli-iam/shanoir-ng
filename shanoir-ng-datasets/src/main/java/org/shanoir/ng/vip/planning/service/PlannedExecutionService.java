package org.shanoir.ng.vip.planning.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.vip.planning.dto.PlannedExecutionDTO;
import org.shanoir.ng.vip.planning.model.PlannedExecution;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface PlannedExecutionService {

    @Async
    void checkForPlannedExecutions(List<DatasetAcquisition> createdAcquisitions);

    List<PlannedExecution> findByStudyId(Long studyId);

    PlannedExecution update(Long plannedExecutionId, PlannedExecutionDTO plannedExecution);

    PlannedExecution findById(Long executionId);

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#plannedExecution.getStudy(), 'CAN_ADMINISTRATE'))")
    void delete(PlannedExecution plannedExecution);

    PlannedExecution save(PlannedExecution plannedExecution);
}
