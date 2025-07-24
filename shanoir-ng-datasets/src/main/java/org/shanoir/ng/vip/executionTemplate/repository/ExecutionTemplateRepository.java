package org.shanoir.ng.vip.executionTemplate.repository;

import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ExecutionTemplateRepository extends CrudRepository<ExecutionTemplate, Long> {

    List<ExecutionTemplate> findByStudyId(Long studyId);

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    void delete(ExecutionTemplate executionTemplate);
}