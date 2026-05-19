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

package org.shanoir.ng.vip.execution.controler;

import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.execution.service.ExecutionServiceImpl;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.dto.VipExecutionDTO;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class ExecutionApiController implements ExecutionApi {

    @Autowired
    private ExecutionServiceImpl executionService;

    @Override
    public ResponseEntity<IdName> createExecution(
            @Parameter(description = "execution", required = true) @RequestBody final ExecutionCandidateDTO candidate) throws EntityNotFoundException, SecurityException, RestServiceException {

        List<Dataset> inputDatasets = executionService.getDatasetsFromParams(candidate.getDatasetParameters());
        IdName createdMonitoring = executionService.createExecution(candidate, inputDatasets);

        return new ResponseEntity<>(createdMonitoring, HttpStatus.OK);
    }

    public ResponseEntity<VipExecutionDTO> getExecution(@Parameter(description = "The execution identifier", required = true) @PathVariable("identifier") String identifier) {
        return ResponseEntity.ok(executionService.getExecution(identifier).block());
    }


    public ResponseEntity<ExecutionStatus> getExecutionStatus(@Parameter(description = "The execution identifier", required = true) @PathVariable("identifier") String identifier) {
        return ResponseEntity.ok(executionService.getExecution(identifier).map(VipExecutionDTO::getStatus).block());
    }

    public ResponseEntity<String> getExecutionStderr(String identifier) {
        return ResponseEntity.ok(executionService.getExecutionStderr(identifier).block());

    }

    public ResponseEntity<String> getExecutionStdout(String identifier) {
        return ResponseEntity.ok(executionService.getExecutionStdout(identifier).block());
    }
}
