package org.shanoir.ng.processing.carmin.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.springframework.security.access.prepost.PreAuthorize;

public interface CarminDatasetProcessingService {

	/**
	 * Find entity by its id. 
	 *
	 * @param id id
	 * @return an entity or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Optional<CarminDatasetProcessing> findById(Long id);
	
	/**
	 * Get all entities.
	 * 
	 * @return a list of manufacturers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<CarminDatasetProcessing> findAll();

	/**
	 * Save an entity.
	 *
	 * @param entity the entity to create.
	 * @return created entity.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #entity.getId() == null")
	CarminDatasetProcessing create(CarminDatasetProcessing entity);

	/**
	 * Update an entity.
	 *
	 * @param entity the entity to update.
	 * @return updated entity.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException 
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	CarminDatasetProcessing update(CarminDatasetProcessing entity) throws EntityNotFoundException;
	
	/**
	 * Delete an entity.
	 * 
	 * @param id the entity id to be deleted.
	 * @throws EntityNotFoundException if the entity cannot be found.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	void deleteById(Long id) throws EntityNotFoundException;
	
    /**
     * save a CarminDatasetProcessing
     * 
     * @param carminDatasetProcessing
     * @return
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #carminDatasetProcessing.getId() == null")
    CarminDatasetProcessing createCarminDatasetProcessing(CarminDatasetProcessing carminDatasetProcessing);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<CarminDatasetProcessing> findAllAllowed();

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Optional<CarminDatasetProcessing> findByIdentifier(String identifier);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    CarminDatasetProcessing updateCarminDatasetProcessing(CarminDatasetProcessing carminDatasetProcessing) throws EntityNotFoundException;

}
