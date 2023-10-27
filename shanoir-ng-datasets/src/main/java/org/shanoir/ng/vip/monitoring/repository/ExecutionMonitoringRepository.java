package org.shanoir.ng.vip.monitoring.repository;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.model.ExecutionStatus;
import org.springframework.data.repository.CrudRepository;

public interface ExecutionMonitoringRepository extends CrudRepository<ExecutionMonitoring, Long> {

    public Optional<ExecutionMonitoring> findByIdentifier(String identifier);

    List<ExecutionMonitoring> findByStatus(ExecutionStatus status);
}
