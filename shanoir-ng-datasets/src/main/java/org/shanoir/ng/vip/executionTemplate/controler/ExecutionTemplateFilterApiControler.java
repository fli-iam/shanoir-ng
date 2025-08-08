package org.shanoir.ng.vip.executionTemplate.controler;

import io.swagger.v3.oas.annotations.Parameter;

import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateFilterDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateFilter;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateFilterRepository;
import org.shanoir.ng.vip.executionTemplate.service.ExecutionTemplateFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/*
    Execution template filter are filters of VIP execution automatically applied after an import.
 */
@Controller
public class ExecutionTemplateFilterApiControler implements ExecutionTemplateFilterApi {

    @Autowired
    ExecutionTemplateFilterService service;

    @Autowired
    ExecutionTemplateFilterRepository repository;

    public ResponseEntity<List<ExecutionTemplateFilterDTO>> getExecutionTemplateFiltersByExecutionTemplateId(@Parameter(description = "The execution template Id", required=true) @PathVariable("executionTemplateId") Long executionTemplateId) {
        List<ExecutionTemplateFilter> filters = repository.findByExecutionTemplate_Id(executionTemplateId);
        return new ResponseEntity<>(ExecutionTemplateFilterDTO.fromEntities(filters), HttpStatus.OK);
    }

    public ResponseEntity<ExecutionTemplateFilterDTO> saveNewExecutionTemplateFilter(@Parameter(description = "The execution template filter to create", required = true) @RequestBody ExecutionTemplateFilterDTO executionTemplateFilterDTO) {
        return new ResponseEntity<ExecutionTemplateFilterDTO>(ExecutionTemplateFilterDTO.fromEntity(repository.save(service.prepareNewEntity(executionTemplateFilterDTO))), HttpStatus.OK);
    }

    public ResponseEntity<Void> deleteExecutionTemplateFilter(@Parameter(description = "The execution template filter Id", required=true) @PathVariable("executionTemplateFilterId") Long executionTemplateFilterId) {
        repository.findById(executionTemplateFilterId).ifPresent(repository::delete);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<ExecutionTemplateFilterDTO> getExecutionTemplateFilterById(@Parameter(description = "The execution template filter Id", required=true) @PathVariable("executionTemplateFilterId") Long executionTemplateFilterId) {
        return new ResponseEntity<>(ExecutionTemplateFilterDTO.fromEntity(repository.findById(executionTemplateFilterId).orElse(null)), HttpStatus.OK);
    }

    public ResponseEntity<ExecutionTemplateFilterDTO> updateExecutionTemplateFilter(
            @Parameter(description = "The execution template filter id", required = true) @PathVariable("executionTemplateFilterId") Long executionTemplateFilterId,
            @Parameter(description = "The execution template filter updated", required = true) @RequestBody ExecutionTemplateFilterDTO executionTemplateFilterDTO) {
        return new ResponseEntity<>(ExecutionTemplateFilterDTO.fromEntity(service.update(executionTemplateFilterDTO)), HttpStatus.OK);
    }
}
