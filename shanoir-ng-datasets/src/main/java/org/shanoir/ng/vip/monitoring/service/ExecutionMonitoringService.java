package org.shanoir.ng.vip.monitoring.service;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.dto.DatasetParameterDTO;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

public interface ExecutionMonitoringService {

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
	 * @param executionMonitoring the entity to create.
	 * @return created entity.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #executionMonitoring.getId() == null")
	ExecutionMonitoring create(ExecutionMonitoring executionMonitoring);

	/**
	 * Update an entity.
	 *
	 * @param executionMonitoring the entity to update.
	 * @return updated entity.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ExecutionMonitoring update(ExecutionMonitoring executionMonitoring) throws EntityNotFoundException;

	/**
	 * Delete an entity.
	 *
	 * @param id the entity id to be deleted.
	 * @throws EntityNotFoundException if the entity cannot be found.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	void deleteById(Long id) throws EntityNotFoundException;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<ExecutionMonitoring> findAllAllowed();

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Optional<ExecutionMonitoring> findByIdentifier(String identifier);

    List<ExecutionMonitoring> findAllRunning();

    List<ParameterResourceDTO> createProcessingResources(ExecutionMonitoring createdProcessing, List<DatasetParameterDTO> parameterDatasets) throws EntityNotFoundException;

	void validateExecutionMonitoring(ExecutionMonitoring executionMonitoring) throws RestServiceException;
}
