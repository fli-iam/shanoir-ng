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

package org.shanoir.ng.vip.executionMonitoring.controller;

import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.repository.ExecutionMonitoringRepository;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.executionMonitoring.dto.ExecutionMonitoringDTO;
import org.shanoir.ng.vip.executionMonitoring.dto.mapper.ExecutionMonitoringMapper;
import org.shanoir.ng.shared.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

/**
 * @author KhalilKes
 */
@Controller
public class ExecutionMonitoringApiController implements ExecutionMonitoringApi {


    @Autowired
    private ExecutionMonitoringService executionMonitoringService;

    @Autowired
    private ExecutionMonitoringRepository executionMonitoringRepository;

    @Autowired
    private ExecutionMonitoringMapper mapper;

    public ResponseEntity<ExecutionMonitoringDTO> findExecutionMonitoringById(Long executionMonitoringId) {
        final Optional<ExecutionMonitoring> monitoring = executionMonitoringRepository.findById(executionMonitoringId);
        return monitoring.map(executionMonitoring -> new ResponseEntity<>(mapper.executionMonitoringToExecutionMonitoringDTO(executionMonitoring), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<List<ExecutionMonitoringDTO>> getAllExecutionMonitoring() {
        final List<ExecutionMonitoring> monitorings = executionMonitoringService.findAllAllowed();

        if (monitorings.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(mapper.executionMonitoringsToExecutionMonitoringDTOs(monitorings), HttpStatus.OK);
    }
}
