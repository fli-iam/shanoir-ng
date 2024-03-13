package org.shanoir.ng.vip.monitoring.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.schedule.ExecutionStatusMonitorService;
import org.shanoir.ng.vip.monitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.dto.ExecutionMonitoringDTO;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.vip.dto.mapper.ExecutionMonitoringMapper;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

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
    private ExecutionMonitoringMapper mapper;

    @Override
    public ResponseEntity<ExecutionMonitoringDTO> findExecutionMonitoringById(Long executionMonitoringId) {
        final Optional<ExecutionMonitoring> monitoring = executionMonitoringService.findById(executionMonitoringId);
        if (monitoring.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(mapper.executionMonitoringToExecutionMonitoringDTO(monitoring.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ExecutionMonitoringDTO>> getAllExecutionMonitoring() {
        final List<ExecutionMonitoring> monitorings = executionMonitoringService.findAllAllowed();
        if (monitorings.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(mapper.executionMonitoringsToExecutionMonitoringDTOs(monitorings), HttpStatus.OK);
    }

    private void validate(BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap(result);
        if (!errors.isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
                    new ErrorDetails(errors));
            throw new RestServiceException(error);
        }
    }


}
