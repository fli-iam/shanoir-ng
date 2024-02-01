package org.shanoir.ng.vip.controller;

import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.monitoring.model.Execution;
import org.shanoir.ng.vip.monitoring.model.ExecutionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VipClientService {

    @Value("${vip.uri}")
    private String VIP_URI;

    private final String VIP_EXECUTION_URI = VIP_URI + "/executions/";

    private final String VIP_PIPELINE_URI = VIP_URI + "/pipelines/";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    /**
     *
     * Get execution from VIP API <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @param identifier
     * @return Execution
     */
    public Execution getExecution(String identifier) {
        String uri = VIP_EXECUTION_URI + identifier;
        ResponseEntity<Execution> execResult = this.restTemplate.exchange(uri, HttpMethod.GET, this.getUserHttpEntity(), Execution.class);
        return execResult.getBody();
    }

    /**
     *
     * Get execution from VIP API <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @return ExecutionDTO
     */
    public ExecutionDTO createExecution(ExecutionDTO execution) {
        HttpEntity<ExecutionDTO> entity = new HttpEntity<>(execution, this.getUserHttpEntity().getHeaders());
        ResponseEntity<ExecutionDTO> execResult = this.restTemplate.exchange(VIP_EXECUTION_URI, HttpMethod.POST, entity, ExecutionDTO.class);
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
        String uri = VIP_EXECUTION_URI + identifier + "/stderr";
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
        String uri = VIP_EXECUTION_URI + identifier + "/stdout";
        ResponseEntity<String> execResult = this.restTemplate.exchange(uri, HttpMethod.GET, this.getUserHttpEntity(), String.class);
        return execResult.getBody();
    }

    /**
     * Get all the user pipelines description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/listPipelines">VIP API</a>
     *
     * @return JSON as string
     */
    public String getPipelineAll() {
        ResponseEntity<String> execResult = this.restTemplate.exchange(VIP_PIPELINE_URI, HttpMethod.GET, this.getUserHttpEntity(), String.class);
        return execResult.getBody();
    }

    /**
     * Get the pipeline description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getPipeline">VIP API</a>
     *
     * @param identifier
     * @return JSON as string
     */
    public String getPipeline(String identifier) {
        String uri = VIP_PIPELINE_URI + identifier;
        ResponseEntity<String> execResult = this.restTemplate.exchange(uri, HttpMethod.GET, this.getUserHttpEntity(), String.class);
        return execResult.getBody();
    }

    /**
     * Prepare the HTTP headers for VIP API call
     * Reuse authenticated user token
     *
     * @return
     * @throws SecurityException
     */
    private HttpEntity<String> getUserHttpEntity() {
        return new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
    }
}
