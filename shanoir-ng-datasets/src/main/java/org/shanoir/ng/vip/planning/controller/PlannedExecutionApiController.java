package org.shanoir.ng.vip.planning.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.planning.dto.PlannedExecutionDTO;
import org.shanoir.ng.vip.planning.dto.mapper.PlannedExecutionMapper;
import org.shanoir.ng.vip.planning.model.PlannedExecution;
import org.shanoir.ng.vip.planning.service.PlannedExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

/*
    Planned execution are VIP execution automatically applied after an import.
 */
@Controller
public class PlannedExecutionApiController implements PlannedExecutionApi {

    @Autowired
    PlannedExecutionService plannedExecutionService;

    @Autowired
    PlannedExecutionMapper peMapper;

    @Override
    public ResponseEntity<List<PlannedExecutionDTO>> getPlannedExecutionsByStudyId (@Parameter(description = "The study Id", required=true) @PathVariable("studyId") Long studyId) {
        List<PlannedExecution> executions = this.plannedExecutionService.findByStudyId(studyId);
        return new ResponseEntity<>(peMapper.PlannedExecutionsToDTOs(executions), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PlannedExecutionDTO> saveNewPlannedExecution(@Parameter(description = "planned execution to create", required = true) @RequestBody PlannedExecution plannedExecution) throws IOException, RestServiceException, SecurityException {

        return new ResponseEntity<PlannedExecutionDTO>(peMapper.PlannedExecutionToDTO(this.plannedExecutionService.save(plannedExecution)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deletePlannedExecution(@Parameter(description = "The PlannedExecution Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException{
        PlannedExecution plannedExecution = this.plannedExecutionService.findById(executionId);
        if (plannedExecution != null) {
            this.plannedExecutionService.delete(plannedExecution);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<PlannedExecutionDTO> getPlannedExecutionById(@Parameter(description = "The PlannedExecution Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException{
        return new ResponseEntity<>(peMapper.PlannedExecutionToDTO(this.plannedExecutionService.findById(executionId)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PlannedExecutionDTO> updatePlannedExecution(
            @Parameter(description = "id of the planned execution", required = true) @PathVariable("executionId") Long executionId,
            @Parameter(description = "center to update", required = true) @RequestBody PlannedExecutionDTO plannedExecution, BindingResult result)
            throws IOException, RestServiceException, EntityNotFoundException, SecurityException {
        return new ResponseEntity<>(peMapper.PlannedExecutionToDTO(this.plannedExecutionService.update(executionId, plannedExecution)), HttpStatus.OK);
    }

}
