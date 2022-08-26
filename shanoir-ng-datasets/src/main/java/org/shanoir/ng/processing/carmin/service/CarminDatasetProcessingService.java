package org.shanoir.ng.processing.carmin.service;

import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.shared.core.service.BasicEntityService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

public interface CarminDatasetProcessingService extends BasicEntityService<CarminDatasetProcessing> {

    /**
     * save a CarminDatasetProcessing
     * 
     * @param carminDatasetProcessing
     * @return
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #carminDatasetProcessing.getId() == null")
    CarminDatasetProcessing createCarminDatasetProcessing(CarminDatasetProcessing carminDatasetProcessing);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Optional<CarminDatasetProcessing> findByIdentifier(String identifier);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    CarminDatasetProcessing updateCarminDatasetProcessing(CarminDatasetProcessing carminDatasetProcessing) throws EntityNotFoundException;

}
