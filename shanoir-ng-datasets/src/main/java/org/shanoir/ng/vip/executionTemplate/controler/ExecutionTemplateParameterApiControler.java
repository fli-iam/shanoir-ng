package org.shanoir.ng.vip.executionTemplate.controler;

import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateParameterDTO;
import org.shanoir.ng.vip.executionTemplate.dto.mapper.ExecutionTemplateParameterMapper;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.shanoir.ng.vip.executionTemplate.service.ExecutionTemplateParameterService;
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
    Execution template parameter are parameters of VIP execution automatically applied after an import.
 */
@Controller
public class ExecutionTemplateParameterApiControler implements ExecutionTemplateParameterApi {

    @Autowired
    ExecutionTemplateParameterService executionTemplateParameterService;

    @Autowired
    ExecutionTemplateParameterMapper etpMapper;

    @Override
    public ResponseEntity<List<ExecutionTemplateParameterDTO>> getExecutionTemplateParametersByExecutionTemplateId (@Parameter(description = "The execution template Id", required=true) @PathVariable("executionTemplateId") Long executionTemplateId) {
        List<ExecutionTemplateParameter> parameters = this.executionTemplateParameterService.findByExecutionTemplateId(executionTemplateId);
        return new ResponseEntity<>(etpMapper.ExecutionTemplateParametersToDTOs(parameters), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ExecutionTemplateParameterDTO> saveNewExecutionTemplateParameter(@Parameter(description = "execution template parameter to create", required = true) @RequestBody ExecutionTemplateParameter executionTemplateParameter) throws IOException, RestServiceException, SecurityException {
        return new ResponseEntity<ExecutionTemplateParameterDTO>(etpMapper.ExecutionTemplateParameterToDTO(this.executionTemplateParameterService.save(executionTemplateParameter)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteExecutionTemplateParameter(@Parameter(description = "The ExecutionTemplateParameter Id", required=true) @PathVariable("executionTemplateParameterId") Long executionTemplateParameterId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException{
        ExecutionTemplateParameter executionTemplateParameter = this.executionTemplateParameterService.findById(executionTemplateParameterId);
        if (executionTemplateParameter != null) {
            this.executionTemplateParameterService.delete(executionTemplateParameter);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<ExecutionTemplateParameterDTO> getExecutionTemplateParameterById(@Parameter(description = "The ExecutionTemplateParameter Id", required=true) @PathVariable("executionTemplateParameterId") Long executionTemplateParameterId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException{
        return new ResponseEntity<>(etpMapper.ExecutionTemplateParameterToDTO(this.executionTemplateParameterService.findById(executionTemplateParameterId)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ExecutionTemplateParameterDTO> updateExecutionTemplateParameter(
            @Parameter(description = "id of the execution template parameter", required = true) @PathVariable("executionTemplateParameterId") Long executionTemplateParameterId,
            @Parameter(description = "center to update", required = true) @RequestBody ExecutionTemplateParameterDTO executionTemplateParameter, BindingResult result)
            throws IOException, RestServiceException, EntityNotFoundException, SecurityException {
        return new ResponseEntity<>(etpMapper.ExecutionTemplateParameterToDTO(this.executionTemplateParameterService.update(executionTemplateParameterId, executionTemplateParameter)), HttpStatus.OK);
    }

}
