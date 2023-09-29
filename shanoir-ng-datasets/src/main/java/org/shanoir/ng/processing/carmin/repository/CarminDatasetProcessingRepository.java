package org.shanoir.ng.processing.carmin.repository;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.ExecutionMonitoring;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.springframework.data.repository.CrudRepository;

public interface CarminDatasetProcessingRepository extends CrudRepository<ExecutionMonitoring, Long> {

    public Optional<ExecutionMonitoring> findByIdentifier(String identifier);

    List<ExecutionMonitoring> findByStatus(ExecutionStatus status);
}
