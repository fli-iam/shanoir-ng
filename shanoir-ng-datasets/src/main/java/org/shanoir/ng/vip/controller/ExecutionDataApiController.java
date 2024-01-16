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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.hibernate.jpa.internal.util.LockOptionsHelper;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.processing.dto.ParameterResourcesDTO;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.dto.ExecutionMonitoringDTO;
import org.shanoir.ng.vip.monitoring.model.*;
import org.shanoir.ng.vip.monitoring.schedule.ExecutionStatusMonitorService;
import org.shanoir.ng.vip.monitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.resource.ProcessingResourceService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.resulthandler.ResultHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Array;
import java.time.LocalDate;
import java.util.*;

@Controller
public class ExecutionDataApiController implements ExecutionDataApi {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionDataApiController.class);

    private static final String DCM = "dcm";
    
    @Autowired
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @Autowired
    private ProcessingResourceService processingResourceService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DatasetSecurityService datasetSecurityService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    @Autowired
    ExecutionMonitoringService executionMonitoringService;

    @Autowired
    ExecutionStatusMonitorService executionStatusMonitorService;

    @Value("${vip.uri}")
    private String VIP_URI;

    @Override
    public ResponseEntity<?> getPath(
            @Parameter(name = "the complete path on which to request information. It can contain non-encoded slashes. Except for the \"exists\" action, any request on a non-existing path should return an error",
                    required=true)
            @PathVariable("completePath") String completePath,
            @NotNull @Parameter(name = "The \"content\" action downloads the raw file. If the path points to a directory, a tarball of this directory is returned. The \"exists\" action returns a BooleanResponse object (see definition) indicating if the path exists or not. The \"properties\" action returns a Path object (see definition) with the path properties. The \"list\" action returns a DirectoryList object (see definition) with the properties of all the files of the directory (if the path is not a directory an error must be returned). The \"md5\" action is optional and returns a PathMd5 object (see definition).",
                    required=true)
            @Valid @RequestParam(value = "action", required = true, defaultValue = "content") String action,
            @Valid @RequestParam(value = "format", required = false, defaultValue = DCM) final String format,
            HttpServletResponse response) throws IOException, RestServiceException, EntityNotFoundException {
        // TODO implement those actions
        switch (action){
            case "exists":
            case "list":
            case "md5":
            case "properties":
                return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
            case "content":

                List<Dataset> datasets = this.processingResourceService.findDatasetsByResourceId(completePath);

                if(datasets.isEmpty()){
                    LOG.error("No dataset found for resource id [{}]", completePath);
                    return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                }

                datasetDownloaderService.massiveDownload(format, datasets, response, true);

                return new ResponseEntity<Void>(HttpStatus.OK);
        }

        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);

    }

    @Override
    public ResponseEntity<ExecutionMonitoring> createExecution(
            @Parameter(name = "execution", required = true) @RequestBody final String executionAsString, @RequestHeader(HttpHeaders.AUTHORIZATION) String authenticationToken) throws EntityNotFoundException, SecurityException {

        authenticationToken = authenticationToken.replace("Bearer ", "").trim();
        // 1: Get dataset IDS and check rights
        List<Long> datasetsIds = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ExecutionDTO execution = null;
        try {
            execution = mapper.readValue(executionAsString, ExecutionDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (ParameterResourcesDTO param : execution.getParametersRessources()) {
            datasetsIds.addAll(param.getDatasetIds());
        }

        if (!this.datasetSecurityService.hasRightOnEveryDataset(datasetsIds, "CAN_IMPORT")) {
            LOG.error("Import right is mandatory for every study we are updating");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        };

        List<Dataset> inputDatasets = this.datasetService.findByIdIn(datasetsIds);

        // 2: Create monitoring on shanoir
        ExecutionMonitoring executionMonitoring = new ExecutionMonitoring();
        executionMonitoring.setName(execution.getName());
        executionMonitoring.setPipelineIdentifier(execution.getPipelineIdentifier());
        executionMonitoring.setResultsLocation(KeycloakUtil.getTokenUserId() + "/" + LocalDate.now());
        executionMonitoring.setTimeout(20);
        executionMonitoring.setStudyId(Long.valueOf(execution.getStudyIdentifier()));
        executionMonitoring.setStatus(ExecutionStatus.RUNNING);
        executionMonitoring.setComment(execution.getName());
        executionMonitoring.setDatasetProcessingType(DatasetProcessingType.valueOf(execution.getProcessingType()));
        executionMonitoring.setOutputProcessing(execution.getOutputProcessing());
        executionMonitoring.setInputDatasets(inputDatasets);

        // Save monitoring in db.
        final ExecutionMonitoring createdMonitoring = executionMonitoringService.create(executionMonitoring);

        List<ParameterResourcesDTO> parametersDatasets = executionMonitoringService.createProcessingResources(createdMonitoring, execution.getParametersRessources());

        // 3: create Execution on VIP
        // init headers with the active access token

        execution.setResultsLocation("shanoir:/" + createdMonitoring.getResultsLocation() + "?token=" + authenticationToken + "&refreshToken=" + execution.getRefreshToken() + "&md5=none&type=File");

        String exportFormat = execution.getExportFormat();
        String extension = ".nii.gz";
        if ("dcm".equals(execution.getExportFormat())) {
            extension = ".zip";
        }

        Map<String, List<String>> parametersDatasetsInputValues = new HashMap<>();
        for (ParameterResourcesDTO parameterResourcesDTO : parametersDatasets) {
            parametersDatasetsInputValues.put(parameterResourcesDTO.getParameter(), new ArrayList<>());

            for (String ressourceId : parameterResourcesDTO.getResourceIds()) {
                String entityName = "resource_id+" + ressourceId + "+" + parameterResourcesDTO.getGroupBy() + extension;
                String inputValue = "shanoir:/" + entityName + "?format=" + exportFormat + "&datasetId=" + ressourceId
                 + "&token=" + authenticationToken + "&refreshToken=" + execution.getRefreshToken() + "&md5=none&type=File";
                parametersDatasetsInputValues.get(parameterResourcesDTO.getParameter()).add(inputValue);
            }
        }
        execution.setInputValues(parametersDatasetsInputValues);

        try {
            LOG.error(mapper.writeValueAsString(execution));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authenticationToken);
        HttpEntity<ExecutionDTO> entity = new HttpEntity<>(execution, headers);

        ResponseEntity<ExecutionDTO> execResult = this.restTemplate.exchange(VIP_URI, HttpMethod.POST, entity, ExecutionDTO.class);

        ExecutionDTO execCreated = execResult.getBody();

        executionMonitoring.setIdentifier(execCreated.getIdentifier());
        executionMonitoring.setStatus(execCreated.getStatus());
        executionMonitoring.setStartDate(execCreated.getStartDate());

        executionMonitoring = this.executionMonitoringService.update(executionMonitoring);
        this.executionStatusMonitorService.startMonitoringJob(executionMonitoring.getIdentifier());

        return new ResponseEntity<>(executionMonitoring, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getexecutionStatus(@Parameter(name = "The execution identifier", required=true) @PathVariable("identifier") String identifier) throws IOException, RestServiceException, EntityNotFoundException, SecurityException {

        AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenResponse.getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Execution execution;
        try {
            String uri = VIP_URI + identifier + "/summary";
            ResponseEntity<Execution> execResult = this.restTemplate.exchange(uri, HttpMethod.GET, entity, Execution.class);
            execution = execResult.getBody();
        } catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(execution.getStatus().getRestLabel(), HttpStatus.OK);
    }
}
