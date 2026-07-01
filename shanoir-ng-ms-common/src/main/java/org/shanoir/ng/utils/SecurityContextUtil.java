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

package org.shanoir.ng.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility class for security context.
 *
 * @author msimon, jlouis
 *
 */
public abstract class SecurityContextUtil {

    /**
     * Clear the authentication
     */
    public static void clearAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Set a @Principal in authentication context.
     *
     * @param role "ROLE_ADMIN" or "ROLE_EXPERT" or ...
     */
    public static void initAuthenticationContext(String role) {
        initAuthenticationContext(role, 92233720L, "mock-token-value");
    }

    public static void initAuthenticationContext(String role, String accessToken) {
        initAuthenticationContext(role, 92233720L, accessToken);
    }

    public static void initAuthenticationContext(String role, String username, Long userId, String accessToken) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority grantedAuth = new SimpleGrantedAuthority(role);
        grantedAuthorities.add(grantedAuth);
        Map<String, Object> claims = Map.of("preferred_username", username, "userId", userId, "realm_access", grantedAuthorities);
        Jwt jwt = new Jwt(accessToken, Instant.now(), Instant.now().plusSeconds(300), Map.of("header", "mock"), claims);
        Authentication authentication = new JwtAuthenticationToken(jwt, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Extract the client the given token was issued to, i.e. its OIDC "azp" (authorized party) claim.
     * Used to refresh/use a user offline token with the exact client it belongs to, so it never has to be
     * configured separately and can never drift from the token. Returns null if the token is not a decodable
     * JWT or carries no azp claim.
     */
    public static String getClientId(String token) {
        Object azp = decodeJwtClaims(token).get("azp");
        return azp != null ? azp.toString() : null;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> decodeJwtClaims(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decoded);
            // Minimal JSON parsing using Jackson which is always on the Spring Boot classpath
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    public static void initAuthenticationContext(String role, Long userId) {
        initAuthenticationContext(role, userId, "mock-token-value");
    }

    public static void initAuthenticationContext(String role, Long userId, String accessToken) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority grantedAuth = new SimpleGrantedAuthority(role);
        grantedAuthorities.add(grantedAuth);
        Map<String, Object> claims = Map.of("preferred_username", "shanoir", "userId", userId, "realm_access", grantedAuthorities);
        Jwt jwt = new Jwt(accessToken, Instant.now(), Instant.now().plusSeconds(300), Map.of("header", "mock"), claims);
        Authentication authentication = new JwtAuthenticationToken(jwt, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
