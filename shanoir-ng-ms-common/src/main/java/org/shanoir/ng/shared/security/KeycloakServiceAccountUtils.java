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

import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.shared.exception.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
    private static final String GRANT_TYPE = "client_credentials";

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
        map.add("client_secret", this.clientSecret);
        map.add("grant_type", this.GRANT_TYPE);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<AccessTokenResponse> response = this.restTemplate.exchange(this.serverUrl, HttpMethod.POST, entity, AccessTokenResponse.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            // in case of error with a response payload.
            LOG.error("Unexpected error while retrieving access token.", e);
            throw new SecurityException("Unexpected error while retrieving access token.", e);
        } catch (RestClientException e) {
            // in case of an error but no response payload;
            LOG.error("No response payload for service account token request", e);
            throw new SecurityException("No response payload for service account token request", e);
        }
    }
}
