package org.shanoir.ng.shared.security;


import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.shared.exception.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Keycloak Service account utility class
 *
 * @author Alae Es-saki
 */
@Component
public class KeycloakServiceAccountUtils {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakServiceAccountUtils.class);
    private final String GRANT_TYPE="client_credentials";

    @Value("${service-account.token.uri:'https://shanoir-ng-nginx/auth'}")
    private String serverUrl;
    @Value("${service-account.client.id:'service-account'}")
    private String clientId;
    @Value("${service-account.client.credential-secret:'SECRET'}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * Get an access token using service account
     *
     * @return AccessTokenResponse
     */
    public AccessTokenResponse getServiceAccountAccessToken() throws SecurityException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", this.clientId);
        map.add("client_secret", "q9STuy_2wLh312ny1BvchG1kv4X5AQ1R6OIjkDqE");
        map.add("grant_type", this.GRANT_TYPE);
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true) // Trust all certificates
                    .build();

            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(
                            SSLConnectionSocketFactoryBuilder.create()
                                    .setSslContext(sslContext)
                                    .build()).build();
            // Create an SSLContext that ignores certificate validation
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            // Set the HttpClient to the RestTemplate
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try{
            ResponseEntity<AccessTokenResponse> response = this.restTemplate.exchange("https://shanoir-ofsep-qualif.irisa.fr/auth/realms/shanoir-ng/protocol/openid-connect/token", HttpMethod.POST, entity, AccessTokenResponse.class);
            return response.getBody();
        } catch (HttpStatusCodeException e){
            // in case of error with a response payload.
            LOG.error("Unexpected error while retrieving access token.", e);
            throw new SecurityException("Unexpected error while retrieving access token.", e);
        }catch (RestClientException e){
            // in case of an error but no response payload;
            LOG.error("No response payload for service account token request", e);
            throw new SecurityException("No response payload for service account token request", e);
        }
    }
}