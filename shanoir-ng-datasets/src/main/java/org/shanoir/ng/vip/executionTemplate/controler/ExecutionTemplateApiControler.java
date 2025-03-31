package org.shanoir.ng.vip.executionTemplate.controler;

import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.dto.mapper.ExecutionTemplateMapper;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.shanoir.ng.vip.executionTemplate.service.ExecutionTemplateService;
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
    Execution template are VIP execution automatically applied after an import.
 */
@Controller
public class ExecutionTemplateApiControler implements ExecutionTemplateApi {

    @Autowired
    ExecutionTemplateService executionTemplateService;

    @Autowired
    ExecutionTemplateRepository repository;

    @Autowired
    ExecutionTemplateMapper etMapper;

    public ResponseEntity<List<ExecutionTemplateDTO>> getExecutionTemplatesByStudyId (@Parameter(description = "The study Id", required=true) @PathVariable("studyId") Long studyId) {
        List<ExecutionTemplate> executions = repository.findByStudyId(studyId);
        return new ResponseEntity<>(etMapper.ExecutionTemplatesToDTOs(executions), HttpStatus.OK);
    }

    public ResponseEntity<ExecutionTemplateDTO> saveNewExecutionTemplate(@Parameter(description = "execution template to create", required = true) @RequestBody ExecutionTemplate executionTemplate) throws IOException, RestServiceException, SecurityException {
        return new ResponseEntity<ExecutionTemplateDTO>(etMapper.ExecutionTemplateToDTO(repository.save(executionTemplate)), HttpStatus.OK);
    }

    public ResponseEntity<Void> deleteExecutionTemplate(@Parameter(description = "The ExecutionTemplate Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException{
        repository.findById(executionId).ifPresent(executionTemplate -> repository.delete(executionTemplate));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<ExecutionTemplateDTO> getExecutionTemplateById(@Parameter(description = "The ExecutionTemplate Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException{
        return new ResponseEntity<>(etMapper.ExecutionTemplateToDTO(repository.findById(executionId).orElse(null)), HttpStatus.OK);
    }

    public ResponseEntity<ExecutionTemplateDTO> updateExecutionTemplate(
            @Parameter(description = "id of the execution template", required = true) @PathVariable("executionId") Long executionId,
            @Parameter(description = "center to update", required = true) @RequestBody ExecutionTemplateDTO executionTemplate, BindingResult result) {
        return new ResponseEntity<>(etMapper.ExecutionTemplateToDTO(executionTemplateService.update(executionId, executionTemplate)), HttpStatus.OK);
    }

}
