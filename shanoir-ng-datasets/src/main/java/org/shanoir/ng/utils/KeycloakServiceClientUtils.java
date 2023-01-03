package org.shanoir.ng.utils;

import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class KeycloakServiceClientUtils {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakServiceClientUtils.class);
    private final String GRANT_TYPE="client_credentials";
    @Autowired
    private RestTemplate restTemplate;
    @Value("${vip.keycloak.token-server-url}")
    private String serverUrl;

    /**
     * Get an access token using a service account
     * @return AccessTokenResponse
     */
    public AccessTokenResponse getServiceAccountAccessToken(String clientId, String clientSecret){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_secret", clientSecret);
        map.add("client_id", clientId);
        map.add("grant_type", this.GRANT_TYPE);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try{
            ResponseEntity<AccessTokenResponse> response = this.restTemplate.exchange(this.serverUrl, HttpMethod.POST, entity, AccessTokenResponse.class);
            LOG.info("token retrieved from client : {}", clientId);

            return response.getBody();
        }catch(HttpStatusCodeException e){
            // in case of error with a response payload.
            LOG.error("error response, message : {}",e.getStatusCode(), e.getMessage());
            e.printStackTrace();
        }catch (RestClientException e){
            // in case of an error but no response payload;
            LOG.error("there is no response payload");
            e.printStackTrace();
        }

        return null;
    }
}
