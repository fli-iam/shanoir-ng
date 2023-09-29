package org.shanoir.ng.processing.carmin.service;

import org.shanoir.ng.processing.carmin.model.ExecutionMonitoring;
import org.shanoir.ng.processing.dto.ParameterResourcesDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

public interface CarminDatasetProcessingService {

	/**
	 * Find entity by its id.
	 *
	 * @param id id
	 * @return an entity or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Optional<ExecutionMonitoring> findById(Long id);

	/**
	 * Get all entities.
	 *
	 * @return a list of manufacturers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<ExecutionMonitoring> findAll();

	/**
	 * Save an entity.
	 *
	 * @param entity the entity to create.
	 * @return created entity.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #entity.getId() == null")
	ExecutionMonitoring create(ExecutionMonitoring entity);

	/**
	 * Update an entity.
	 *
	 * @param entity the entity to update.
	 * @return updated entity.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ExecutionMonitoring update(ExecutionMonitoring entity) throws EntityNotFoundException;

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
     * @param executionMonitoring
     * @return
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #executionMonitoring.getId() == null")
	ExecutionMonitoring createCarminDatasetProcessing(ExecutionMonitoring executionMonitoring);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<ExecutionMonitoring> findAllAllowed();

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Optional<ExecutionMonitoring> findByIdentifier(String identifier);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ExecutionMonitoring updateCarminDatasetProcessing(ExecutionMonitoring executionMonitoring) throws EntityNotFoundException;

    List<ExecutionMonitoring> findAllRunning();

    List<ParameterResourcesDTO> createProcessingResources(ExecutionMonitoring createdProcessing, List<ParameterResourcesDTO> parameterDatasets);
}
