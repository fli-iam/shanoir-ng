package org.shanoir.ng.vip.executionMonitoring.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.repository.ExecutionMonitoringRepository;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.executionMonitoring.dto.ExecutionMonitoringDTO;
import org.shanoir.ng.vip.executionMonitoring.dto.mapper.ExecutionMonitoringMapper;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.*;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionTrackingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;

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
    private ExecutionTrackingServiceImpl executionTrackingService;

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

    public ResponseEntity<Void> getTrackingFile(String pipeLineName, HttpServletResponse response) throws RestServiceException {
        executionTrackingService.downloadTrackingFile(pipeLineName, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
