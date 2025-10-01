package org.shanoir.ng.vip.executionTemplate.repository;

import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateFilter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ExecutionTemplateFilterRepository extends CrudRepository<ExecutionTemplateFilter, Long> {

    List<ExecutionTemplateFilter> findByExecutionTemplate_Id(Long executionTemplateId);

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplateFilter.executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    void delete(ExecutionTemplateFilter executionTemplateFilter);
}