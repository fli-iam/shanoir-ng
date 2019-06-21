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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.shanoir.ng.shared.exception.TokenNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * Utility class for Keycloak requests.
 * 
 * @author msimon
 *
 */
public final class KeycloakUtil {

	public static final String USER_ID_TOKEN_ATT = "userId";

	/**
	 * Get current user roles from Keycloak token.
	 * 
	 * @return user roles.
	 * @throws ShanoirStudiesException
	 */
	public static Set<String> getTokenRoles() {
		final KeycloakSecurityContext context = getKeycloakSecurityContext();
		final AccessToken accessToken = context.getToken();
		if (accessToken == null) {
			throw new TokenNotFoundException("Access token not found");
		}
		return accessToken.getRealmAccess().getRoles();
	}
	
	/**
	 * Get connected user roles. If anonymous user, returns an empty list.
	 * 
	 * @return roles
	 */
	@SuppressWarnings("rawtypes")
	public static Collection<String> getConnectedUserRoles() {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			return new ArrayList<String>();
		} else {
			final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal == null) {
				throw new IllegalArgumentException("connectedUser cannot be null");
			}
			if (principal instanceof User) {
				final List<String> userRoles = new ArrayList<String>();
				for (GrantedAuthority authority : ((User) principal).getAuthorities()) {
					userRoles.add(authority.getAuthority());
				}
				return userRoles;
			}
			return ((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getRealmAccess().getRoles();
		}
	}

	/**
	 * Get current user id from Keycloak token.
	 * 
	 * @return user id.
	 * @throws ShanoirStudiesException
	 */
	public static Long getTokenUserId() {
		final KeycloakSecurityContext context = getKeycloakSecurityContext();
		final AccessToken accessToken = context.getToken();
		if (accessToken == null) {
			throw new TokenNotFoundException("Access token not found");
		}
		final Map<String, Object> otherClaims = accessToken.getOtherClaims();
		if (otherClaims.containsKey(USER_ID_TOKEN_ATT)) {
			return Long.valueOf(otherClaims.get(USER_ID_TOKEN_ATT).toString());
		}
		return null;
	}

	/**
	 * Get headers with current Keycloak token.
	 * 
	 * @return HTTP headers.
	 * @throws ShanoirStudiesException
	 */
	public static HttpHeaders getKeycloakHeader() {
		final KeycloakSecurityContext context = getKeycloakSecurityContext();
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Bearer " + context.getTokenString());
		return headers;
	}

	/**
	 * Get current access token.
	 * 
	 * @return access token.
	 * @throws ShanoirStudiesException
	 */
	@SuppressWarnings("rawtypes")
	private static KeycloakSecurityContext getKeycloakSecurityContext() throws SecurityException {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			throw new SecurityException("Anonymous user");
		}
		final KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		return principal.getKeycloakSecurityContext();
	}

}
