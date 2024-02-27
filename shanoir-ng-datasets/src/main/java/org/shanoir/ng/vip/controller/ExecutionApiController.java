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

package org.shanoir.ng.vip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.dto.DatasetParameterDTO;
import org.shanoir.ng.vip.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.dto.VipExecutionDTO;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.monitoring.schedule.ExecutionStatusMonitorService;
import org.shanoir.ng.vip.monitoring.service.ExecutionMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ExecutionApiController implements ExecutionApi {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionApiController.class);
    public static final String SHANOIR_URI_SCHEME = "shanoir:/";

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DatasetSecurityService datasetSecurityService;

    @Autowired
    private ExecutionMonitoringService executionMonitoringService;

    @Autowired
    private ExecutionStatusMonitorService executionStatusMonitorService;

    @Autowired
    private VipClientService vipClient;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * Create execution on VIP and return Shanoir linked execution monitoring
     *
     * @param executionAsString
     * @return
     * @throws EntityNotFoundException
     * @throws SecurityException
     */
    @Override
    public ResponseEntity<IdName> createExecution(
            @Parameter(name = "execution", required = true) @RequestBody final String executionAsString) throws EntityNotFoundException, SecurityException {

        // 1: Get dataset IDS and check rights
        List<Long> datasetsIds = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ExecutionCandidateDTO candidate;
        try {
            candidate = mapper.readValue(executionAsString, ExecutionCandidateDTO.class);
        } catch (JsonProcessingException e) {
            LOG.error("Could not parse execution DTO from input, please respect the expected structure.", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        for (DatasetParameterDTO param : candidate.getDatasetParameters()) {
            datasetsIds.addAll(param.getDatasetIds());
        }

        if (!this.datasetSecurityService.hasRightOnEveryDataset(datasetsIds, "CAN_IMPORT")) {
            LOG.error("Import right is mandatory for every study we are updating");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        };

        List<Dataset> inputDatasets = datasetService.findByIdIn(datasetsIds);

        ExecutionMonitoring executionMonitoring = this.createExecutionMonitoring(candidate, inputDatasets);

        VipExecutionDTO execCreated = this.createVipExecution(candidate, executionMonitoring).block();

        if(execCreated == null){
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(this.updateAndStartExecutionMonitoring(executionMonitoring, execCreated), HttpStatus.OK);
    }

    /**
     * Update monitoring with vip execution details and persist it in DB
     *
     * @param executionMonitoring
     * @param execCreated
     * @return
     * @throws EntityNotFoundException
     */
    private IdName updateAndStartExecutionMonitoring(ExecutionMonitoring executionMonitoring, VipExecutionDTO execCreated) throws EntityNotFoundException, SecurityException {
        executionMonitoring.setIdentifier(execCreated.getIdentifier());
        executionMonitoring.setStatus(execCreated.getStatus());
        executionMonitoring.setStartDate(execCreated.getStartDate());
        ExecutionMonitoring createdMonitoring = this.executionMonitoringService.update(executionMonitoring);
        executionStatusMonitorService.startMonitoringJob(createdMonitoring, null);
        return new IdName(createdMonitoring.getId(), createdMonitoring.getName());
    }

    /**
     * Create execution into VIP and return created execution
     *
     * @param candidate
     * @param executionMonitoring
     * @return
     */
    private Mono<VipExecutionDTO> createVipExecution(ExecutionCandidateDTO candidate, ExecutionMonitoring executionMonitoring) {
        VipExecutionDTO dto = new VipExecutionDTO();
        dto.setIdentifier(candidate.getIdentifier());
        dto.setName(candidate.getName());
        dto.setPipelineIdentifier(candidate.getPipelineIdentifier());
        dto.setStudyIdentifier(candidate.getStudyIdentifier().toString());

        dto.setResultsLocation(this.getResultsLocationUri(executionMonitoring.getResultsLocation(), candidate));

        dto.setInputValues(this.getInputValues(executionMonitoring, candidate));

        return vipClient.createExecution(dto);
    }

    /**
     * Set non-file parameters and processed datasets parameters as URIs
     *
     * @param createdMonitoring
     * @param candidate
     * @return
     */
    private Map<String, java.lang.Object> getInputValues(ExecutionMonitoring createdMonitoring, ExecutionCandidateDTO candidate) {

        Map<String, Object> inputValues = new HashMap<>(candidate.getInputParameters());

        List<ParameterResourceDTO> parametersDatasets = executionMonitoringService.createProcessingResources(createdMonitoring, candidate.getDatasetParameters());

        Map<String, List<String>> inputDatasets = new HashMap<>();

        for (ParameterResourceDTO parameterResourcesDTO : parametersDatasets) {

            String groupBy = parameterResourcesDTO.getGroupBy().name().toLowerCase();
            String exportFormat = parameterResourcesDTO.getExportFormat();

            inputDatasets.put(parameterResourcesDTO.getParameter(), new ArrayList<>());

            for (String resourceId : parameterResourcesDTO.getResourceIds()) {
                String inputValue = this.getInputValueUri(candidate, groupBy, exportFormat, resourceId, KeycloakUtil.getToken());
                inputDatasets.get(parameterResourcesDTO.getParameter()).add(inputValue);
            }
        }
        inputValues.putAll(inputDatasets);

        return inputValues;
    }

    private String getResultsLocationUri(String resultLocation, ExecutionCandidateDTO candidate) {
        return SHANOIR_URI_SCHEME + resultLocation
                + "?token=" + KeycloakUtil.getToken()
                + "&refreshToken=" + candidate.getRefreshToken()
                + "&clientId=" + candidate.getClient()
                + "&md5=none&type=File";
    }

    private String getInputValueUri(ExecutionCandidateDTO execution, String groupBy, String exportFormat, String resourceId, String authenticationToken){
        String entityName = "resource_id+" + resourceId + "+" + groupBy + ("dcm".equals(exportFormat) ? ".zip" : ".nii.gz");
        return SHANOIR_URI_SCHEME + entityName
                + "?format=" + exportFormat
                + "&resourceId=" + resourceId
                + "&token=" + authenticationToken
                + "&refreshToken=" + execution.getRefreshToken()
                + "&clientId=" + execution.getClient()
                + "&md5=none&type=File";
    }

    /**
     * Create execution monitoring
     *
     * @param execution
     * @param inputDatasets
     * @return
     */
    private ExecutionMonitoring createExecutionMonitoring(ExecutionCandidateDTO execution, List<Dataset> inputDatasets) {
        ExecutionMonitoring executionMonitoring = new ExecutionMonitoring();
        executionMonitoring.setName(execution.getName());
        executionMonitoring.setPipelineIdentifier(execution.getPipelineIdentifier());
        executionMonitoring.setResultsLocation(KeycloakUtil.getTokenUserId() + "/" + formatter.format(LocalDateTime.now()));
        executionMonitoring.setTimeout(20);
        executionMonitoring.setStudyId(Long.valueOf(execution.getStudyIdentifier()));
        executionMonitoring.setStatus(ExecutionStatus.RUNNING);
        executionMonitoring.setComment(execution.getName());
        executionMonitoring.setDatasetProcessingType(DatasetProcessingType.valueOf(execution.getProcessingType()));
        executionMonitoring.setOutputProcessing(execution.getOutputProcessing());
        executionMonitoring.setInputDatasets(inputDatasets);
        return this.executionMonitoringService.create(executionMonitoring);
    }

    @Override
    public ResponseEntity<VipExecutionDTO> getExecution(@Parameter(name = "The execution identifier", required=true) @PathVariable("identifier") String identifier) {
        return ResponseEntity.ok(vipClient.getExecution(identifier).block());
    }


    @Override
    public ResponseEntity<ExecutionStatus> getExecutionStatus(@Parameter(name = "The execution identifier", required=true) @PathVariable("identifier") String identifier) {
        return ResponseEntity.ok(vipClient.getExecution(identifier).map(VipExecutionDTO::getStatus).block());
    }

    @Override
    public ResponseEntity<String> getExecutionStderr(String identifier) {
        return ResponseEntity.ok(vipClient.getExecutionStderr(identifier).block());

    }

    @Override
    public ResponseEntity<String> getExecutionStdout(String identifier) {
        return ResponseEntity.ok(vipClient.getExecutionStdout(identifier).block());
    }
}
