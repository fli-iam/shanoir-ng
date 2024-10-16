package org.shanoir.ng.vip.planning.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.planning.model.PlannedExecution;
import org.shanoir.ng.vip.planning.service.PlannedExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

/*
    Planned execution are VIP execution atuomatically applied after an import.
 */
@Service
public class PlannedExecutionApiController implements PlannedExecutionApi {

    @Autowired
    PlannedExecutionService plannedExecutionService;

    @Override
    public ResponseEntity<List<PlannedExecution>> getPlannedExecutionsByStudyId (@Parameter(description = "The study Id", required=true) @PathVariable("studyId") Long studyId) {
        List<PlannedExecution> executions = this.plannedExecutionService.findByStudyId(studyId);
        return new ResponseEntity<>(executions, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PlannedExecution> createPlannedExecution(@RequestBody PlannedExecution plannedExecution) throws IOException, RestServiceException, SecurityException {
        return new ResponseEntity<PlannedExecution>(this.plannedExecutionService.save(plannedExecution), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<Void> deletePlannedExecution(@Parameter(description = "The PlannedExecution Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException{
        this.plannedExecutionService.delete(executionId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
    @Override
    public ResponseEntity<PlannedExecution> getPlannedExecutionById(@Parameter(description = "The PlannedExecution Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException{
        return new ResponseEntity<PlannedExecution>(this.plannedExecutionService.findById(executionId), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<PlannedExecution> updatePlannedExecution(@RequestBody PlannedExecution plannedExecution) throws IOException, RestServiceException, EntityNotFoundException, SecurityException {
        return new ResponseEntity<PlannedExecution>(this.plannedExecutionService.update(plannedExecution), HttpStatus.OK);
    }

}
