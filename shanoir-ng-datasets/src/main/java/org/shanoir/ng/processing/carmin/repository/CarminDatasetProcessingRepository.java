package org.shanoir.ng.processing.carmin.repository;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.springframework.data.repository.CrudRepository;

public interface CarminDatasetProcessingRepository extends CrudRepository<CarminDatasetProcessing, Long> {

    public Optional<CarminDatasetProcessing> findByIdentifier(String identifier);

    List<CarminDatasetProcessing> findByStatus(ExecutionStatus status);
}
