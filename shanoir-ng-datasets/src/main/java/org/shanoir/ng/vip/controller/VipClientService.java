package org.shanoir.ng.vip.controller;

import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.monitoring.model.Execution;
import org.shanoir.ng.vip.monitoring.model.ExecutionDTO;
import org.shanoir.ng.vip.resulthandler.ResultHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class VipClientService {

    private static final Logger LOG = LoggerFactory.getLogger(VipClientService.class);

    @Value("${vip.uri}")
    private String vipBaseUrl;

    private String vipExecutionUrl = vipBaseUrl + "executions/";

    private String vipPipelineUrl = vipBaseUrl + "pipelines/";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    /**
     *
     * Get execution from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @param identifier
     * @return Execution
     */
    public Execution getExecution(String identifier) {
        String uri = vipExecutionUrl + identifier;
        ResponseEntity<Execution> execResult = this.restTemplate.exchange(uri, HttpMethod.GET, this.getUserHttpEntity(), Execution.class);
        return execResult.getBody();
    }

    /**
     * Try to get execution from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     * Authenticate as service account
     * @param attempts
     * @param identifier
     * @return
     * @throws ResultHandlerException
     */
    public Execution getExecutionAsService(int attempts, String identifier) throws ResultHandlerException, SecurityException {

        String uri = vipExecutionUrl + identifier;

        HttpEntity<String> httpHeaders = this.getServiceHttpEntity();

        // check how many times the loop tried to get the execution's info without success (only UNAUTHORIZED error)
        if(attempts >= 3){
            throw new ResultHandlerException("Failed to get execution details from VIP in [" + attempts + "] attempts", null);
        }

        try {
            ResponseEntity<Execution> executionResponseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, httpHeaders, Execution.class);
            return executionResponseEntity.getBody();
        } catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                LOG.info("Unauthorized : refreshing token... ({} attempts)", attempts);
                return null;
            } else {
                throw new ResultHandlerException("Failed to get execution details from VIP in " + attempts + " attempts", e);
            }
        } catch (RestClientException e) {
            throw new ResultHandlerException("No response payload in execution infos from VIP", e);
        }
    }

    /**
     *
     * Create execution on <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @return ExecutionDTO
     */
    public ExecutionDTO createExecution(ExecutionDTO execution) {
        HttpEntity<ExecutionDTO> entity = new HttpEntity<>(execution, this.getUserHttpEntity().getHeaders());
        ResponseEntity<ExecutionDTO> execResult = this.restTemplate.exchange(vipExecutionUrl, HttpMethod.POST, entity, ExecutionDTO.class);
        return execResult.getBody();
    }

    /**
     *
     * Get execution stderr logs from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getStderr">VIP API</a>
     *
     * @param identifier
     * @return string
     */
    public String getExecutionStderr(String identifier) {
        String uri = vipExecutionUrl + identifier + "/stderr";
        ResponseEntity<String> execResult = this.restTemplate.exchange(uri, HttpMethod.GET, this.getUserHttpEntity(), String.class);
        return execResult.getBody();
    }

    /**
     *
     * Get execution stdout logs from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getStdout">VIP API</a>
     *
     * @param identifier
     * @return string
     */
    public String getExecutionStdout(String identifier) {
        String uri = vipExecutionUrl + identifier + "/stdout";
        ResponseEntity<String> execResult = this.restTemplate.exchange(uri, HttpMethod.GET, this.getUserHttpEntity(), String.class);
        return execResult.getBody();
    }

    /**
     * Get all the user pipelines description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/listPipelines">VIP API</a>
     *
     * @return JSON as string
     */
    public String getPipelineAll() {
        ResponseEntity<String> execResult = this.restTemplate.exchange(vipPipelineUrl, HttpMethod.GET, this.getUserHttpEntity(), String.class);
        return execResult.getBody();
    }

    /**
     * Get the pipeline description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getPipeline">VIP API</a>
     *
     * @param identifier
     * @return JSON as string
     */
    public String getPipeline(String identifier) {
        String uri = vipPipelineUrl + identifier;
        ResponseEntity<String> execResult = this.restTemplate.exchange(uri, HttpMethod.GET, this.getUserHttpEntity(), String.class);
        return execResult.getBody();
    }


    /**
     * Set up the HTTP headers for VIP API call
     * Refresh & use service account token
     *
     * @return
     * @throws SecurityException
     */
    private HttpEntity<String> getServiceHttpEntity() throws SecurityException {
        AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();
        String token = accessTokenResponse.getToken();
        // init headers with the active access token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    /**
     * Set up the HTTP headers for VIP API call
     * Reuse authenticated user token
     *
     * @return
     * @throws SecurityException
     */
    private HttpEntity<String> getUserHttpEntity() {
        return new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
    }
}
