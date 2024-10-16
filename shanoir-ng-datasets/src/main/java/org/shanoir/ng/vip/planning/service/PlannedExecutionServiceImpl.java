package org.shanoir.ng.vip.planning.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.vip.planning.model.PlannedExecution;
import org.shanoir.ng.vip.planning.repository.PlannedExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlannedExecutionServiceImpl implements PlannedExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(PlannedExecutionServiceImpl.class);

    @Autowired
    private PlannedExecutionRepository plannedExecutionRepository;

    @Override
    public List<PlannedExecution> findByStudyId(Long studyId) {
        return plannedExecutionRepository.findByStudyId(studyId);
    }

    @Override
    public PlannedExecution update(PlannedExecution plannedExecution) {
        return this.plannedExecutionRepository.save(plannedExecution);
    }

    @Override
    public PlannedExecution findById(Long executionId) {
        return this.plannedExecutionRepository.findById(executionId).orElse(null);
    }

    @Override
    public void delete(Long executionId) {
        PlannedExecution execution = this.findById(executionId);
        if (execution == null) {
            // Already deleted
            return;
        }
        this.plannedExecutionRepository.delete(execution);
    }

    @Override
    public PlannedExecution save(PlannedExecution plannedExecution) {
        return this.plannedExecutionRepository.save(plannedExecution);
    }

    /**
     * This method is called aynchroneously at the end of the import to check if a planned execution has to be done.
     * @param createdAcquisitions the list of acqusitions to check for planned executions
     */
    @Override
    @Async
    public void checkForPlannedExecutions(List<DatasetAcquisition> createdAcquisitions) {
        if (createdAcquisitions == null || createdAcquisitions.isEmpty()) {
            LOG.error("No data imported, no planned execution.");
            return;
        }
        // Retrieve the list of potential executions to plan
        List<PlannedExecution> potentialPlannedExecutions = this.findByStudyId(createdAcquisitions.get(0).getExamination().getStudyId());

        // Filter the executions to apply
        List<PlannedExecution> executionsToApply = new ArrayList<>();
        for (PlannedExecution potentialPlannedExecution : potentialPlannedExecutions) {
            if (filterExecution(potentialPlannedExecution, createdAcquisitions)) {
                executionsToApply.add(potentialPlannedExecution);
            }
        }

        if (executionsToApply.isEmpty()) {
            LOG.error("No executions filter match the import, no planned execution.");
            return;
        }

        // Apply all filtered executions
        for(PlannedExecution executionToApply : executionsToApply) {
            applyExecution(executionToApply, createdAcquisitions);
        }
    }

    private boolean filterExecution(PlannedExecution potentialPlannedExecution, List<DatasetAcquisition> createdAcquisitions) {
        // TODO: complete
        return true;
    }

    private void applyExecution(PlannedExecution executionToApply, List<DatasetAcquisition> createdAcquisitions) {
        // Create an execution and infer the logic
    }
}
