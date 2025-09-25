package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.springframework.scheduling.annotation.Async;
import java.util.List;

public interface ExecutionTemplateService {

    /**
     * This method is called asynchroneously at the end of the import to check if an execution template has to be done.
     * @param createdAcquisitions the list of acqusitions to check for execution templates
     */
    @Async
    void createExecutionsFromExecutionTemplates(List<DatasetAcquisition> createdAcquisitions);


    /**
     * This method allows the template filters management while updating template
     * @param executionTemplate the newly created template without filters
     *
     * @return the newly created template with its filters
     */
    ExecutionTemplate update(ExecutionTemplate executionTemplate);
}