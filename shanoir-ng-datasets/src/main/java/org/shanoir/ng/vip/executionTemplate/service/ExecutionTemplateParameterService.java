package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateParameterDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

public interface ExecutionTemplateParameterService {

    @Async
    List<ExecutionTemplateParameter> findByExecutionTemplateId(Long executionTemplateId);

    ExecutionTemplateParameter update(Long executionTemplateParameterId, ExecutionTemplateParameterDTO executionTemplateParameterDTO);

    ExecutionTemplateParameter findById(Long parameterId);

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplateParameter.getExecutionTemplateId(), 'CAN_ADMINISTRATE'))")
    void delete(ExecutionTemplateParameter executionTemplateParameter);

    ExecutionTemplateParameter save(ExecutionTemplateParameter executionTemplateParameter);
}
