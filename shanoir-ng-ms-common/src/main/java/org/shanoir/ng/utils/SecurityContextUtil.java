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

    private static final long PRINCIPAL_USER_ID = 92233720L;

    private static final int TOKEN_LIFESPAN = 300;

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
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        GrantedAuthority grantedAuth = new SimpleGrantedAuthority(role);
        grantedAuthorities.add(grantedAuth);
        Map<String, Object> claims = Map.of("preferred_username", "shanoir", "userId", PRINCIPAL_USER_ID, "realm_access", grantedAuthorities);
        Jwt jwt = new Jwt("mock-token-value", Instant.now(), Instant.now().plusSeconds(TOKEN_LIFESPAN), Map.of("header", "mock"), claims);
        Authentication authentication = new JwtAuthenticationToken(jwt, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
