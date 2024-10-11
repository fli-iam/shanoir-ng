package org.shanoir.ng.vip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.dto.VipExecutionDTO;
import org.shanoir.ng.vip.resulthandler.ResultHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class VipClientService {

    private static final Logger LOG = LoggerFactory.getLogger(VipClientService.class);

    @Value("${vip.uri}")
    private String vipUrl;

    private final String vipExecutionUri = "/executions/";

    private final String vipPipelineUri = "/pipelines/";

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.create(vipUrl);
    }

    @Autowired
    ObjectMapper mapper;

    /**
     *
     * Get execution from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @param identifier
     * @return Execution
     */
    public Mono<VipExecutionDTO> getExecution(String identifier) {
        String url = vipExecutionUri + identifier;
        return webClient.get()
            .uri(url)
            .headers(headers -> headers.addAll(this.getUserHttpHeaders()))
            .retrieve()
            .bodyToMono(VipExecutionDTO.class)
            .onErrorResume(e -> {
                ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get execution [" + identifier + "] from VIP API", e.getMessage());
                return Mono.error(new RestServiceException(e, model));
            });
    }

    /**
     * Try to get execution from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     * Authenticate as service account
     * @param attempts
     * @param identifier
     * @return
     * @throws ResultHandlerException
     */
    public Mono<VipExecutionDTO> getExecutionAsServiceAccount(int attempts, String identifier) throws ResultHandlerException, SecurityException {

        if(attempts >= 3){
            throw new ResultHandlerException("Failed to get execution details from VIP in [" + attempts + "] attempts", null);
        }

        String url = vipExecutionUri + identifier + "/summary";

        HttpHeaders headers = this.getServiceAccountHttpHeaders();

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
     * Create execution on <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @return ExecutionDTO
     */
    public Mono<VipExecutionDTO> createExecution(VipExecutionDTO execution) {
        try {
            LOG.error("Created execution: " + mapper.writeValueAsString(execution));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return webClient.post()
            .uri(vipExecutionUri)
            .headers(headers -> headers.addAll(this.getUserHttpHeaders()))
            .bodyValue(execution)
            .retrieve()
            .bodyToMono(VipExecutionDTO.class).onErrorResume(e -> {
                ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't create execution [" + execution.getName() + "] through VIP API", e.getMessage());
                return Mono.error(new RestServiceException(e, model));
            });
    }

    /**
     * Get execution stderr logs from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getStderr">VIP API</a>
     *
     * @param identifier
     * @return string
     */
    public Mono<String> getExecutionStderr(String identifier) {

        String url = vipExecutionUri + identifier + "/stderr";
        return webClient.get()
            .uri(url)
            .headers(headers -> headers.addAll(this.getUserHttpHeaders()))
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(e -> {
                ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get execution [" + identifier + "] stderr logs from VIP API", e.getMessage());
                return Mono.error(new RestServiceException(e, model));
            });
    }

    /**
     * Get execution stdout logs from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getStdout">VIP API</a>
     *
     * @param identifier
     * @return string
     */
    public Mono<String> getExecutionStdout(String identifier) {
        String url = vipExecutionUri + identifier + "/stdout";
        return webClient.get()
            .uri(url)
            .headers(headers -> headers.addAll(this.getUserHttpHeaders()))
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(e -> {
                ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get execution [" + identifier + "] stdout logs from VIP API", e.getMessage());
                return Mono.error(new RestServiceException(e, model));
            });
    }

    /**
     * Get all the user pipelines description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/listPipelines">VIP API</a>
     *
     * @return JSON as string
     */
    public Mono<String> getPipelineAll() {
        return webClient.get()
            .uri(vipPipelineUri)
            .headers(headers -> headers.addAll(this.getUserHttpHeaders()))
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(e -> {
                ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get pipelines descriptions from VIP API", e.getMessage());
                return Mono.error(new RestServiceException(e, model));
            });
    }

    /**
     * Get the pipeline description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getPipeline">VIP API</a>
     *
     * @param identifier
     * @return JSON as string
     */
    public Mono<String> getPipeline(String identifier, String version) {
        String url = vipPipelineUri + identifier + "/" + version;
        return webClient.get()
            .uri(url)
            .headers(headers -> headers.addAll(this.getUserHttpHeaders()))
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(e -> {
                ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get pipeline [" + identifier + "/" + version + "] description from VIP API", e.getMessage());
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

    /**
     * Set up the HTTP headers for VIP API call
     * Reuse authenticated user token
     *
     * @return
     * @throws SecurityException
     */
    private HttpHeaders getUserHttpHeaders() {
        return KeycloakUtil.getKeycloakHeader();
    }
}
