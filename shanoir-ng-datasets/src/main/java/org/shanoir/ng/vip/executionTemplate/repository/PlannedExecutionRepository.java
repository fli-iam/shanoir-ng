package org.shanoir.ng.vip.executionTemplate.repository;

import org.shanoir.ng.vip.executionTemplate.model.PlannedExecution;
import org.springframework.data.repository.CrudRepository;

public interface PlannedExecutionRepository extends CrudRepository<PlannedExecution, Long> {

    void deleteByAcquisitionIdAndTemplateId(Long acquisitionId, Long templateId);
}
