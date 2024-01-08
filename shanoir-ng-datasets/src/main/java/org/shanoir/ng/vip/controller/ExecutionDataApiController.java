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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Value(value = "vip.uri")
    private String vipExecutionRestApi;

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
    public ResponseEntity<?> createExecution(
            @Valid @RequestParam("execution") final ExecutionDTO execution,
            @Valid @RequestParam("datasetsIds") final List<Long> datasetsIds
            ) throws EntityNotFoundException, SecurityException {
        // 1: Get dataset IDS and check rights
        LOG.error("" + execution);
        LOG.error("" + datasetsIds);
        if (!this.datasetSecurityService.hasRightOnEveryDataset(datasetsIds, "CAN_IMPORT")) {
            LOG.error("Admin right is mandatory for every study we are updating");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        };

        List<Dataset> inputDatasets = this.datasetService.findByIdIn(datasetsIds);

        // 2: Create monitoring on shanoir
        ExecutionMonitoring executionMonitoring = new ExecutionMonitoring();
        executionMonitoring.setName(execution.getName());
        executionMonitoring.setPipelineIdentifier(execution.getPipelineIdentifier());
        executionMonitoring.setResultsLocation(KeycloakUtil.getTokenUserId() + "/" + LocalDate.now());
        executionMonitoring.setTimeout(20);
        executionMonitoring.setStatus(ExecutionStatus.RUNNING);
        executionMonitoring.setComment(execution.getName());
        executionMonitoring.setDatasetProcessingType(DatasetProcessingType.valueOf(execution.getProcessingType()));
        executionMonitoring.setOutputProcessing(execution.getOutputProcessing());
        executionMonitoring.setInputDatasets(inputDatasets);

        /* Save monitoring in db. */
        final ExecutionMonitoring createdMonitoring = executionMonitoringService.create(executionMonitoring);

        List<ParameterResourcesDTO> parametersDatasets = executionMonitoringService.createProcessingResources(createdMonitoring, execution.getParametersRessources());

        executionStatusMonitorService.startMonitoringJob(createdMonitoring.getIdentifier());

        // 3: create Execution on VIP
        // init headers with the active access token
        AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();

        execution.setResultsLocation("shanoir:/" + executionMonitoring.getResultsLocation() + "?token=" + accessTokenResponse.getToken() + "&refreshToken=" + accessTokenResponse.getRefreshToken() + "&md5=none&type=File");

        Map<String, List<String>> parametersDatasetsInputValues = new HashMap<>();
        for (ParameterResourcesDTO parameterResourcesDTO : parametersDatasets) {
            parametersDatasetsInputValues.put(parameterResourcesDTO.getParameter(), parameterResourcesDTO.getResourceIds());
        }
        execution.setInputValues(parametersDatasetsInputValues);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenResponse.getToken());
        HttpEntity<ExecutionDTO> entity = new HttpEntity<>(execution, headers);
        this.restTemplate.exchange(vipExecutionRestApi, HttpMethod.POST, entity, ExecutionDTO.class);

        return null;
    }

}
