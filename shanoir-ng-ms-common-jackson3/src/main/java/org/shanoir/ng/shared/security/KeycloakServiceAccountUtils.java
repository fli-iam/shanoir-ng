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

package org.shanoir.ng.shared.security;

import java.time.Duration;
import java.util.Map;

import org.shanoir.ng.shared.exception.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Keycloak Service account utility class
 *
 * @author Alae Es-saki
 */
@Component
public class KeycloakServiceAccountUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakServiceAccountUtils.class);

    private static final String GRANT_TYPE = "client_credentials";

    @Value("${service-account.token.uri:'https://shanoir-ng-nginx/auth'}")
    private String serverUrl;

    @Value("${service-account.client.id:'service-account'}")
    private String clientId;

    @Value("${service-account.client.credential-secret:'SECRET'}")
    private String clientSecret;

    @Autowired
    private WebClient webClient;

    /**
     * Get an access token using service account credentials.
     *
     * @return a Map containing the token response fields (access_token, expires_in, etc.)
     * @throws SecurityException if the token request fails
     */
    public Map<String, Object> getServiceAccountAccessToken() throws SecurityException {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", this.clientId);
        form.add("client_secret", this.clientSecret);
        form.add("grant_type", GRANT_TYPE);

        try {
            Map<String, Object> response = webClient.post()
                    .uri(this.serverUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .cast(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .block(Duration.ofSeconds(10));

            if (response == null || !response.containsKey("access_token")) {
                throw new SecurityException("Empty or invalid token response from Keycloak.");
            }
            return response;

        } catch (WebClientResponseException e) {
            // Error with a response payload (4xx, 5xx)
            LOG.error("Unexpected error while retrieving access token. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new SecurityException("Unexpected error while retrieving access token.", e);

        } catch (SecurityException e) {
            throw e;

        } catch (Exception e) {
            // No response payload or connection error
            LOG.error("No response payload for service account token request.", e);
            throw new SecurityException("No response payload for service account token request.", e);
        }
    }

    /**
     * Convenience method to extract the raw access token string.
     *
     * @return the access token string
     * @throws SecurityException if the token request fails
     */
    public String getAccessTokenString() throws SecurityException {
        return (String) getServiceAccountAccessToken().get("access_token");
    }

}
