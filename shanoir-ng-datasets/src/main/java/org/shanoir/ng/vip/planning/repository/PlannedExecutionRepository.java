package org.shanoir.ng.vip.planning.repository;

import org.shanoir.ng.vip.planning.model.PlannedExecution;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlannedExecutionRepository extends CrudRepository<PlannedExecution, Long> {

    List<PlannedExecution> findByStudyId(Long studyId);

}
