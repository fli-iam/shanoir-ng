package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.springframework.scheduling.annotation.Async;
import java.util.List;

public interface ExecutionTemplateService {

    /**
     * This method is called aynchroneously at the end of the import to check if an execution template has to be done.
     * @param createdAcquisitions the list of acqusitions to check for execution templates
     */
    @Async
    void createExecutionsFromExecutionTemplates(List<DatasetAcquisition> createdAcquisitions);
    /**
     * This method is called aynchroneously at the end of the import to check if an execution template has to be done.
     * @param executionTemplateId the ExecutionTEmplate id to modify in DB
     * @param executionTemplateDTO the modified by user ExecutionTemplate
     * @return the updated ExecutionTemplate
     */
    ExecutionTemplate update(Long executionTemplateId, ExecutionTemplateDTO executionTemplateDTO);

}
