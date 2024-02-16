package org.shanoir.ng.vip.controller;

import jakarta.annotation.PostConstruct;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.dto.VipExecutionDTO;
import org.shanoir.ng.vip.resulthandler.ResultHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class VipClientService {

    private static final Logger LOG = LoggerFactory.getLogger(VipClientService.class);

    @Value("${vip.uri}/executions/")
    private String vipExecutionUrl;

    @Value("${vip.uri}/pipelines/")
    private String vipPipelineUrl;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.create();
    }

    /**
     *
     * Get execution from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @param identifier
     * @return Execution
     */
    public Mono<VipExecutionDTO> getExecution(String identifier) {
        String url = vipExecutionUrl + identifier;
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> this.getUserHttpHeaders())
                .retrieve()
                .bodyToMono(VipExecutionDTO.class);
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

        String url = vipExecutionUrl + identifier;

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
                .bodyToMono(VipExecutionDTO.class);


//        // check how many times the loop tried to get the execution's info without success (only UNAUTHORIZED error)
//        if(attempts >= 3){
//            throw new ResultHandlerException("Failed to get execution details from VIP in [" + attempts + "] attempts", null);
//        }
//
//        try {
//            ResponseEntity<VipExecutionDTO> executionResponseEntity = this.restTemplate.exchange(url, HttpMethod.GET, headers, VipExecutionDTO.class);
//            return executionResponseEntity.getBody();
//        } catch (HttpStatusCodeException e) {
//            // in case of an error with response payload
//            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
//                LOG.info("Unauthorized : refreshing token... ({} attempts)", attempts);
//                return null;
//            } else {
//                throw new ResultHandlerException("Failed to get execution details from VIP in " + attempts + " attempts", e);
//            }
//        } catch (RestClientException e) {
//            throw new ResultHandlerException("No response payload in execution infos from VIP", e);
//        }
    }

    /**
     * Create execution on <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @return ExecutionDTO
     */
    public Mono<VipExecutionDTO> createExecution(VipExecutionDTO execution) {

        String url = vipExecutionUrl;
        return webClient.post()
                .uri(url)
                .headers(httpHeaders -> this.getUserHttpHeaders())
                .bodyValue(execution)
                .retrieve()
                .bodyToMono(VipExecutionDTO.class);
    }

    /**
     * Get execution stderr logs from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getStderr">VIP API</a>
     *
     * @param identifier
     * @return string
     */
    public Mono<String> getExecutionStderr(String identifier) {

        String url = vipExecutionUrl + identifier + "/stderr";
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> this.getUserHttpHeaders())
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Get execution stdout logs from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getStdout">VIP API</a>
     *
     * @param identifier
     * @return string
     */
    public Mono<String> getExecutionStdout(String identifier) {
        String url = vipExecutionUrl + identifier + "/stdout";
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> this.getUserHttpHeaders())
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Get all the user pipelines description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/listPipelines">VIP API</a>
     *
     * @return JSON as string
     */
    public Mono<String> getPipelineAll() {
        return webClient.get()
                .uri(vipPipelineUrl)
                .headers(httpHeaders -> this.getUserHttpHeaders())
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Get the pipeline description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getPipeline">VIP API</a>
     *
     * @param name
     * @param version
     * @return JSON as string
     */
    public Mono<String> getPipeline(String name, String version) {
        String url = vipPipelineUrl + name + "/" + version;
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> this.getUserHttpHeaders())
                .retrieve()
                .bodyToMono(String.class);
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
