package org.shanoir.ng.vip.executionMonitoring.repository;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.springframework.data.repository.CrudRepository;

public interface ExecutionMonitoringRepository extends CrudRepository<ExecutionMonitoring, Long> {

    Optional<ExecutionMonitoring> findByIdentifier(String identifier);

    List<ExecutionMonitoring> findByStatus(ExecutionStatus status);
}
