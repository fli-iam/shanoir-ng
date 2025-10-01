/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.vip.executionMonitoring.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

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
