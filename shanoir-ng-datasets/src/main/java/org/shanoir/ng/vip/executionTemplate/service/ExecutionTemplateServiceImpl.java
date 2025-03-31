package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExecutionTemplateServiceImpl implements ExecutionTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTemplateServiceImpl.class);

    @Autowired
    private ExecutionTemplateRepository repository;

    public ExecutionTemplate update(Long executionTemplateId, ExecutionTemplateDTO executionTemplate) {
        ExecutionTemplate dbExecution = repository.findById(executionTemplateId).orElse(null);

        if (dbExecution == null) {
            return null;
        }

        // Update updatable fields only
        dbExecution.setName(executionTemplate.getName());

        return this.repository.save(dbExecution);
    }

    @Async
    public void createExecutionsFromExecutionTemplates(List<DatasetAcquisition> createdAcquisitions) {
        if (createdAcquisitions == null || createdAcquisitions.isEmpty()) {
            LOG.error("No data imported, no pipeline execution.");
            return;
        }

        List<ExecutionTemplate> executionTemplates = repository.findByStudyId(createdAcquisitions.get(0).getExamination().getStudyId());

        Map<Long, ExecutionTemplate> executionsToApply = new HashMap<>();
        for (DatasetAcquisition acquisition : createdAcquisitions) {
            if (filterExecutionTemplate(executionTemplates, acquisition)) {
                //executionsToApply.add(potentialExecutionTemplate);
            }
        }

        if (executionsToApply.isEmpty()) {
            LOG.error("No executions filter match the import, no pipeline execution.");
            return;
        }

        for(Long acquisitionId : executionsToApply.keySet()) {
            //applyExecution(executionToApply, createdAcquisitions);
        }
    }

    /**
     * This method filters the execution templates available for the given acquisition
     */
    private boolean filterExecutionTemplate(List<ExecutionTemplate> executionTemplates, DatasetAcquisition acquisition) {
        // TODO: complete
        return true;
    }

    /**
     * This method launch executions according to the execution template for all the given dataset acquisition
     */
    private void applyExecution(ExecutionTemplate executionToApply, List<DatasetAcquisition> createdAcquisitions) {
        // Create an execution and infer the logic
    }
}
