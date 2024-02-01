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
import org.shanoir.ng.processing.dto.ParameterResourcesDTO;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.monitoring.model.Execution;
import org.shanoir.ng.vip.monitoring.model.ExecutionDTO;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.monitoring.schedule.ExecutionStatusMonitorService;
import org.shanoir.ng.vip.monitoring.service.ExecutionMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpStatusCodeException;

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

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Override
    public ResponseEntity<ExecutionDTO> createExecution(
            @Parameter(name = "execution", required = true) @RequestBody final String executionAsString) throws EntityNotFoundException, SecurityException {

        String authenticationToken = KeycloakUtil.getToken();

        // 1: Get dataset IDS and check rights
        List<Long> datasetsIds = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ExecutionDTO execution = null;
        try {
            execution = mapper.readValue(executionAsString, ExecutionDTO.class);
        } catch (JsonProcessingException e) {
            LOG.error("Could not parse execution DTO from input, please respect the expected structure.", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String clientId = execution.getClient();

        for (ParameterResourcesDTO param : execution.getParametersRessources()) {
            datasetsIds.addAll(param.getDatasetIds());
        }

        if (!this.datasetSecurityService.hasRightOnEveryDataset(datasetsIds, "CAN_IMPORT")) {
            LOG.error("Import right is mandatory for every study we are updating");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        };

        List<Dataset> inputDatasets = this.datasetService.findByIdIn(datasetsIds);

        // 2: Create monitoring on shanoir
        ExecutionMonitoring executionMonitoring = this.createExecutionMonitoring(execution, inputDatasets);

        // Save monitoring in db.
        final ExecutionMonitoring createdMonitoring = executionMonitoringService.create(executionMonitoring);
        // 3: create Execution on VIP
        // init headers with the active access token

        execution.setResultsLocation(this.getResultsLocationUri(createdMonitoring.getResultsLocation(), authenticationToken, execution.getRefreshToken(), clientId));

        Map<String, List<String>> parametersDatasetsInputValues = getParametersDatasetsInputValues(createdMonitoring, execution, authenticationToken);
        execution.setInputValues(parametersDatasetsInputValues);

        ExecutionDTO execCreated = vipClient.createExecution(execution);

        executionMonitoring.setIdentifier(execCreated.getIdentifier());
        executionMonitoring.setStatus(execCreated.getStatus());
        executionMonitoring.setStartDate(execCreated.getStartDate());

        executionMonitoring = this.executionMonitoringService.update(executionMonitoring);

        this.executionStatusMonitorService.startMonitoringJob(executionMonitoring, null);

        return new ResponseEntity<>(execCreated, HttpStatus.OK);
    }

    private Map<String, List<String>> getParametersDatasetsInputValues(ExecutionMonitoring createdMonitoring, ExecutionDTO execution, String authenticationToken) {
        List<ParameterResourcesDTO> parametersDatasets = executionMonitoringService.createProcessingResources(createdMonitoring, execution.getParametersRessources());

        Map<String, List<String>> parametersDatasetsInputValues = new HashMap<>();

        for (ParameterResourcesDTO parameterResourcesDTO : parametersDatasets) {

            String groupBy = parameterResourcesDTO.getGroupBy().name().toLowerCase();

            parametersDatasetsInputValues.put(parameterResourcesDTO.getParameter(), new ArrayList<>());

            for (String ressourceId : parameterResourcesDTO.getResourceIds()) {
                String inputValue = this.getInputValueUri(execution, groupBy, ressourceId, authenticationToken);
                parametersDatasetsInputValues.get(parameterResourcesDTO.getParameter()).add(inputValue);
            }
        }
        return parametersDatasetsInputValues;
    }

    private String getResultsLocationUri(String resultLocation, String authenticationToken, String refreshToken, String clientId) {
        return SHANOIR_URI_SCHEME + resultLocation
                + "?token=" + authenticationToken
                + "&refreshToken=" + refreshToken
                + "&clientId=" + clientId
                + "&md5=none&type=File";
    }

    private String getInputValueUri(ExecutionDTO execution, String groupBy, String ressourceId, String authenticationToken){

        String exportFormat = execution.getExportFormat();

        String entityName = "resource_id+" + ressourceId + "+" + groupBy + ("dcm".equals(exportFormat) ? ".zip" : ".nii.gz");

        return SHANOIR_URI_SCHEME + entityName
                + "?format=" + exportFormat
                + "&datasetId=" + ressourceId
                + "&token=" + authenticationToken
                + "&refreshToken=" + execution.getRefreshToken()
                + "&clientId=" + execution.getClient()
                + "&md5=none&type=File";
    }

    private ExecutionMonitoring createExecutionMonitoring(ExecutionDTO execution, List<Dataset> inputDatasets) {
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
        return executionMonitoring;
    }

    @Override
    public ResponseEntity<Execution> getExecution(@Parameter(name = "The execution identifier", required=true) @PathVariable("identifier") String identifier) {
        Execution execution;
        try {
            execution = vipClient.getExecution(identifier);
        } catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(execution, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<String> getExecutionStatus(@Parameter(name = "The execution identifier", required=true) @PathVariable("identifier") String identifier) {
        Execution execution;
        try {
            execution = vipClient.getExecution(identifier);
        } catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(execution.getStatus().getRestLabel(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getExecutionStderr(String identifier) {
        String stderr;
        try {
            stderr = vipClient.getExecutionStderr(identifier);
        } catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(stderr, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<String> getExecutionStdout(String identifier) {
        String stdout;
        try {
            stdout = vipClient.getExecutionStdout(identifier);
        } catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(stdout, HttpStatus.OK);
    }
}
