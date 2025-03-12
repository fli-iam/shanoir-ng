package org.shanoir.ng.vip.executionMonitoring.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.shared.dto.DatasetParameterDTO;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ExecutionMonitoringService {

	/**
	 * Create execution monitoring
	 *
	 * @param execution
	 * @param inputDatasets
	 * @return the created execution monitoring
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ExecutionMonitoring createExecutionMonitoring(ExecutionCandidateDTO execution, List<Dataset> inputDatasets) throws RestServiceException;

	/**
	 * Update an execution monitoring.
	 *
	 * @param executionMonitoring the entity to update.
	 * @return updated execution monitoring.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ExecutionMonitoring update(ExecutionMonitoring executionMonitoring) throws EntityNotFoundException;

	/**
	 * Find all allowed execution monitoring
	 *
	 * @return list of all allowed executing monitoring for the current user.
	 */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<ExecutionMonitoring> findAllAllowed();

	/**
	 * Async job that monitor the state of the VIP execution and process its outcome
	 *
	 * @param processing
	 * @param event
	 */
	void startMonitoringJob(ExecutionMonitoring processing, ShanoirEvent event) throws EntityNotFoundException, SecurityException;
}
