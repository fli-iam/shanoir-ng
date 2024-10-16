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

import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
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
     * @param candidate
     * @return
     * @throws EntityNotFoundException
     * @throws SecurityException
     */
    @Override
    public ResponseEntity<IdName> createExecution(
            @Parameter(description = "execution", required = true) @RequestBody final ExecutionCandidateDTO candidate) throws EntityNotFoundException, SecurityException, RestServiceException {

        // 1: Get dataset and check rights
        List<Dataset> inputDatasets = this.getDatasetsFromParams(candidate.getDatasetParameters());
        this.checkRightsForExecution(inputDatasets);

        ExecutionMonitoring executionMonitoring = this.createExecutionMonitoring(candidate, inputDatasets);

        VipExecutionDTO createdExecution = this.createVipExecution(candidate, executionMonitoring);

        IdName createdMonitoring = this.updateAndStartExecutionMonitoring(executionMonitoring, createdExecution);

        return new ResponseEntity<>(createdMonitoring, HttpStatus.OK);
    }

    private List<Dataset> getDatasetsFromParams(List<DatasetParameterDTO> parameters) {
        List<Long> datasetsIds = new ArrayList<>();
        for (DatasetParameterDTO param : parameters) {
            datasetsIds.addAll(param.getDatasetIds());
        }
        return datasetService.findByIdIn(datasetsIds);
    }

    private void checkRightsForExecution(List<Dataset> datasets) throws EntityNotFoundException, RestServiceException {
        if (!this.datasetSecurityService.hasRightOnEveryDataset(datasets.stream().map(Dataset::getId).toList(), "CAN_ADMINISTRATE")) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNAUTHORIZED.value(),
                            "You don't have the right to run pipelines on studies you don't administrate."));
        }
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
    private VipExecutionDTO createVipExecution(ExecutionCandidateDTO candidate, ExecutionMonitoring executionMonitoring) throws EntityNotFoundException {
        VipExecutionDTO dto = new VipExecutionDTO();
        dto.setName(candidate.getName());
        dto.setPipelineIdentifier(candidate.getPipelineIdentifier());
        dto.setStudyIdentifier(candidate.getStudyIdentifier().toString());

        dto.setResultsLocation(this.getResultsLocationUri(executionMonitoring.getResultsLocation(), candidate));

        dto.setInputValues(this.getInputValues(executionMonitoring, candidate));

        return vipClient.createExecution(dto).block();
    }

    /**
     * Set non-file parameters and processed datasets parameters as URIs
     *
     * @param createdMonitoring
     * @param candidate
     * @return
     */
    private Map<String, java.lang.Object> getInputValues(ExecutionMonitoring createdMonitoring, ExecutionCandidateDTO candidate) throws EntityNotFoundException {

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

    private String getInputValueUri(ExecutionCandidateDTO candidate, String groupBy, String exportFormat, String resourceId, String authenticationToken) {
        String entityName = "resource_id+" + resourceId + "+" + groupBy + ("dcm".equals(exportFormat) ? ".zip" : ".nii.gz");
        return SHANOIR_URI_SCHEME + entityName
                + "?format=" + exportFormat
                + "&resourceId=" + resourceId
                + "&token=" + authenticationToken
                + (candidate.getConverterId() != null ? ("&converterId=" + candidate.getConverterId()) : "")
                + "&refreshToken=" + candidate.getRefreshToken()
                + "&clientId=" + candidate.getClient()
                + "&md5=none&type=File";
    }

    /**
     * Create execution monitoring
     *
     * @param execution
     * @param inputDatasets
     * @return
     */
    private ExecutionMonitoring createExecutionMonitoring(ExecutionCandidateDTO execution, List<Dataset> inputDatasets) throws RestServiceException {
        ExecutionMonitoring executionMonitoring = new ExecutionMonitoring();
        executionMonitoring.setName(execution.getName());
        executionMonitoring.setPipelineIdentifier(execution.getPipelineIdentifier());
        executionMonitoring.setResultsLocation(KeycloakUtil.getTokenUserId() + "/" + formatter.format(LocalDateTime.now()));
        executionMonitoring.setTimeout(20);
        executionMonitoring.setStudyId(execution.getStudyIdentifier());
        executionMonitoring.setStatus(ExecutionStatus.RUNNING);
        executionMonitoring.setComment(execution.getName());
        executionMonitoring.setDatasetProcessingType(DatasetProcessingType.valueOf(execution.getProcessingType()));
        executionMonitoring.setOutputProcessing(execution.getOutputProcessing());
        executionMonitoring.setInputDatasets(inputDatasets);
        executionMonitoring.setUsername(KeycloakUtil.getTokenUserName());
        executionMonitoringService.validateExecutionMonitoring(executionMonitoring);
        return this.executionMonitoringService.create(executionMonitoring);
    }

    @Override
    public ResponseEntity<VipExecutionDTO> getExecution(@Parameter(description = "The execution identifier", required = true) @PathVariable("identifier") String identifier) {
        return ResponseEntity.ok(vipClient.getExecution(identifier).block());
    }


    @Override
    public ResponseEntity<ExecutionStatus> getExecutionStatus(@Parameter(description = "The execution identifier", required = true) @PathVariable("identifier") String identifier) {
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

