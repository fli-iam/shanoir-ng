package org.shanoir.ng.vip.execution.service;

import jakarta.annotation.PostConstruct;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.dto.VipExecutionDTO;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringServiceImpl;
import org.shanoir.ng.vip.processingResource.service.ProcessingResourceServiceImpl;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.shared.dto.DatasetParameterDTO;
import org.shanoir.ng.vip.shared.service.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionServiceImpl.class);

    @Value("${vip.shanoir-vip-host}")
    private String SHANOIR_URI_SCHEME_LOCAL;

    public static String SHANOIR_URI_SCHEME;

    private final String vipExecutionUri = "/executions";

    private WebClient webClient;

    @Value("${vip.uri}")
    private String vipUrl;

    @Autowired
    private ExecutionMonitoringServiceImpl executionMonitoringService;

    @Autowired
    private ProcessingResourceServiceImpl processingResourceService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DatasetSecurityService datasetSecurityService;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    @Autowired
    private ExecutionTrackingServiceImpl executionTrackingService;

    @Autowired
    private Utils utils;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.create(vipUrl);
        SHANOIR_URI_SCHEME = (SHANOIR_URI_SCHEME_LOCAL.contains(".") ? SHANOIR_URI_SCHEME_LOCAL.substring(0, SHANOIR_URI_SCHEME_LOCAL.indexOf('.')).replaceAll("-","") : "local") + ":/";
    }

    public IdName createExecution(ExecutionCandidateDTO candidate, List<Dataset> inputDatasets) throws SecurityException, EntityNotFoundException, RestServiceException {
        ExecutionMonitoring executionMonitoring = executionMonitoringService.createExecutionMonitoring(candidate, inputDatasets);
        executionTrackingService.updateTrackingFile(executionMonitoring, ExecutionTrackingServiceImpl.execStatus.VALID);

        VipExecutionDTO createdExecution = createVipExecution(candidate, executionMonitoring);
        executionTrackingService.updateTrackingFile(executionMonitoring, ExecutionTrackingServiceImpl.execStatus.SENT);
        return updateAndStartExecutionMonitoring(executionMonitoring, createdExecution);
    }

    public List<Dataset> getDatasetsFromParams(List<DatasetParameterDTO> parameters){
        List<Long> datasetsIds = new ArrayList<>();
        for (DatasetParameterDTO param : parameters) {
            datasetsIds.addAll(param.getDatasetIds());
        }
        return datasetService.findByIdIn(datasetsIds);
    }

    public void checkRightsForExecution(List<Dataset> datasets) throws EntityNotFoundException, RestServiceException {
        if (!datasetSecurityService.hasRightOnEveryDataset(datasets.stream().map(Dataset::getId).toList(), "CAN_ADMINISTRATE")) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNAUTHORIZED.value(),
                            "You don't have the right to run pipelines on studies you don't administrate."));
        }
    }

    public Mono<VipExecutionDTO> getExecution(String identifier) {
        String url = vipExecutionUri + "/" + identifier;
        return webClient.get()
                .uri(url)
                .headers(headers -> headers.addAll(utils.getUserHttpHeaders()))
                .retrieve()
                .bodyToMono(VipExecutionDTO.class)
                .onErrorResume(e -> {
                    ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get execution [" + identifier + "] from VIP API", e.getMessage());
                    return Mono.error(new RestServiceException(e, model));
                });
    }

    public Mono<String> getExecutionStderr(String identifier) {

        String url = vipExecutionUri + "/" + identifier + "/stderr";
        return webClient.get()
                .uri(url)
                .headers(headers -> headers.addAll(utils.getUserHttpHeaders()))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get execution [" + identifier + "] stderr logs from VIP API", e.getMessage());
                    return Mono.error(new RestServiceException(e, model));
                });
    }

    public Mono<String> getExecutionStdout(String identifier) {
        String url = vipExecutionUri + "/" + identifier + "/stdout";
        return webClient.get()
                .uri(url)
                .headers(headers -> headers.addAll(utils.getUserHttpHeaders()))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get execution [" + identifier + "] stdout logs from VIP API", e.getMessage());
                    return Mono.error(new RestServiceException(e, model));
                });
    }

    public Mono<VipExecutionDTO> getExecutionAsServiceAccount(int attempts, String identifier) throws ResultHandlerException, SecurityException {

        if(attempts >= 3){
            throw new ResultHandlerException("Failed to get execution details from VIP in [" + attempts + "] attempts", null);
        }

        String url = vipExecutionUri + "/" + identifier + "/summary";
        HttpHeaders headers = getServiceAccountHttpHeaders();

        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                        LOG.info("Unauthorized : refreshing token... ({} attempts)", attempts);
                        return Mono.empty();
                    }
                    return Mono.error(new ResultHandlerException("Failed to get execution details from VIP in " + attempts + " attempts", null));
                })
                .bodyToMono(VipExecutionDTO.class)
                .onErrorResume(e -> {
                    ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get execution [" + identifier + "] from VIP API", e.getMessage());
                    return Mono.error(new RestServiceException(e, model));
                });
    }

    /**
     * Update monitoring with vip execution details and persist it in DB
     */
    private IdName updateAndStartExecutionMonitoring(ExecutionMonitoring executionMonitoring, VipExecutionDTO execCreated) throws EntityNotFoundException, SecurityException {
        executionMonitoring.setIdentifier(execCreated.getIdentifier());
        executionMonitoring.setStatus(execCreated.getStatus());
        executionMonitoring.setStartDate(execCreated.getStartDate());
        ExecutionMonitoring createdMonitoring = executionMonitoringService.update(executionMonitoring);
        executionMonitoringService.startMonitoringJob(createdMonitoring, null);
        return new IdName(createdMonitoring.getId(), createdMonitoring.getName());
    }

    /**
     * Create execution into VIP and return created execution
     */
    private VipExecutionDTO createVipExecution(ExecutionCandidateDTO candidate, ExecutionMonitoring executionMonitoring) throws EntityNotFoundException {
        VipExecutionDTO dto = new VipExecutionDTO();
        dto.setName(candidate.getName());
        dto.setPipelineIdentifier(candidate.getPipelineIdentifier());
        dto.setStudyIdentifier(candidate.getStudyIdentifier().toString());

        dto.setResultsLocation(getResultsLocationUri(executionMonitoring.getResultsLocation(), candidate));

        dto.setInputValues(getInputValues(executionMonitoring, candidate));

        return createExecution(dto)
                .onErrorMap(WebClientResponseException.BadRequest.class, ex ->
                        new RuntimeException("HTTP Error while communicating with VIP :  - " + ex.getResponseBodyAsString()))
                .block();
    }

    /**
     * Set non-file parameters and processed datasets parameters as URIs
     */
    private Map<String, Object> getInputValues(ExecutionMonitoring createdMonitoring, ExecutionCandidateDTO candidate) throws EntityNotFoundException {

        Map<String, Object> inputValues = new HashMap<>(candidate.getInputParameters());
        Map<String, List<String>> inputDatasets = new HashMap<>();

        List<ParameterResourceDTO> parametersDatasets = processingResourceService.createProcessingResources(createdMonitoring, candidate.getDatasetParameters());

        for (ParameterResourceDTO parameterResourcesDTO : parametersDatasets) {
            String groupBy = parameterResourcesDTO.getGroupBy().name().toLowerCase();
            String exportFormat = parameterResourcesDTO.getExportFormat();
            inputDatasets.put(parameterResourcesDTO.getParameter(), new ArrayList<>());

            for (String resourceId : parameterResourcesDTO.getResourceIds()) {
                String inputValue = getInputValueUri(candidate, groupBy, exportFormat, resourceId, KeycloakUtil.getToken());
                inputDatasets.get(parameterResourcesDTO.getParameter()).add(inputValue);
            }
        }
        inputValues.putAll(inputDatasets);

        return inputValues;
    }


    /**
     * Get location of exec results as URI
     */
    private String getResultsLocationUri(String resultLocation, ExecutionCandidateDTO candidate) {
        return SHANOIR_URI_SCHEME + resultLocation
                + "?token=" + KeycloakUtil.getToken()
                + "&refreshToken=" + candidate.getRefreshToken()
                + "&clientId=" + candidate.getClient()
                + "&md5=none&type=File";
    }

    /**
     * Get input values of exec as URI
     */
    private String getInputValueUri(ExecutionCandidateDTO candidate, String groupBy, String exportFormat, String resourceId, String authenticationToken){
        String entityName = "resource_id+" + resourceId + "+" + groupBy + ("dcm".equals(exportFormat) ? ".zip" : ".nii.gz");
        return SHANOIR_URI_SCHEME + entityName
                + "?format=" + exportFormat
                + "&resourceId=" + resourceId
                + "&token=" + authenticationToken
                + (candidate.getConverterId()  != null ? ("&converterId=" + candidate.getConverterId()) : "")
                + "&refreshToken=" + candidate.getRefreshToken()
                + "&clientId=" + candidate.getClient()
                + "&md5=none&type=File";
    }

    /**
     * Create execution on <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @return ExecutionDTO
     */
    private Mono<VipExecutionDTO> createExecution(VipExecutionDTO execution) {

        return webClient.post()
                .uri(vipExecutionUri)
                .headers(headers -> headers.addAll(utils.getUserHttpHeaders()))
                .bodyValue(execution)
                .retrieve()
                .bodyToMono(VipExecutionDTO.class).onErrorResume(e -> {
                    ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't create execution [" + execution.getName() + "] through VIP API", e.getMessage());
                    return Mono.error(new RestServiceException(e, model));
                });
    }

    /**
     * Set up the HTTP headers for VIP API call
     * Refresh & use service account token
     *
     * @return
     * @throws SecurityException
     */
    private HttpHeaders getServiceAccountHttpHeaders() throws SecurityException {
        AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();
        String token = accessTokenResponse.getToken();

        // init headers with the active access token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
