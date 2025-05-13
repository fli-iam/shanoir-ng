package org.shanoir.ng.vip.executionTemplate.repository;

import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ExecutionTemplateParameterRepository extends CrudRepository<ExecutionTemplateParameter, Long> {

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    void delete(ExecutionTemplateParameter executionTemplateParameter);
}
