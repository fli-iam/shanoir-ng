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
import java.util.stream.Collectors;

import org.shanoir.ng.shared.exception.TokenNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility class for Keycloak requests.
 * 
 * @author msimon
 *
 */
public final class KeycloakUtil {
	
	private static final String PREFERRED_USERNAME = "preferred_username";

	private static final String CAN_IMPORT_FROM_PACS = "canImportFromPACS";
	
	public static final String USER_ID_TOKEN_ATT = "userId";

	/**
	 * Get current user roles from Keycloak token.
	 * 
	 * @return user roles.
	 * @throws ShanoirStudiesException
	 */
	public static Set<String> getTokenRoles() {
		final JwtAuthenticationToken jwt = getJwtAuthenticationToken();
		if (jwt == null) {
			throw new TokenNotFoundException("JwtAuthenticationToken not found");
		}
		return jwt.getAuthorities().stream().map(a -> a.toString()).collect(Collectors.toSet());
	}
	
	/**
	 * Know if connected user can import from PACS
	 * 
	 * @return a boolean
	 */
	public static boolean canImportFromPACS() {
		final JwtAuthenticationToken jwt = getJwtAuthenticationToken();
		return Boolean.parseBoolean(jwt.getToken().getClaims().get(CAN_IMPORT_FROM_PACS).toString());
	}
	
	/**
	 * Get connected user roles. If anonymous user, returns an empty list.
	 * 
	 * @return roles
	 */
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
			return getJwtAuthenticationToken().getAuthorities().stream().map(a -> a.toString()).collect(Collectors.toSet());
		}
	}

	/**
	 * Get current user id from Keycloak token.
	 * 
	 * @return user id.
	 * @throws ShanoirStudiesException
	 */
	public static Long getTokenUserId() {
		final JwtAuthenticationToken jwt = getJwtAuthenticationToken();
		if (jwt == null) {
			throw new TokenNotFoundException("JwtAuthenticationToken not found.");
		}
		final Map<String, Object> claims = jwt.getToken().getClaims();
		if (claims.containsKey(USER_ID_TOKEN_ATT)) {
			return Long.valueOf(claims.get(USER_ID_TOKEN_ATT).toString());
		}
		return null;
	}
	
	public static String getTokenUserName() {
		final JwtAuthenticationToken jwt = getJwtAuthenticationToken();
		if (jwt == null) {
			throw new TokenNotFoundException("JwtAuthenticationToken not found.");
		}
		return jwt.getToken().getClaim(PREFERRED_USERNAME);
	}

	public static String getToken() {
		final JwtAuthenticationToken jwt = getJwtAuthenticationToken();
		if (jwt == null) {
			throw new TokenNotFoundException("JwtAuthenticationToken not found.");
		}
		return jwt.getToken().getTokenValue();
	}

	/**
	 * Get current ID from keycloak with a default fallback value
	 * @param defaultId the default value to set
	 * @return The keycloak current user ID, the default ID otherwise.
	 */
	public static Long getTokenUserId(Long defaultId) {
		try {
			return getTokenUserId();
		} catch (Exception e) {
			return defaultId;
		}
	}

	/**
	 * Get headers with current Keycloak token.
	 * 
	 * @return HTTP headers.
	 * @throws ShanoirStudiesException
	 */
	public static HttpHeaders getKeycloakHeader() {
		final JwtAuthenticationToken jwt = getJwtAuthenticationToken();
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Bearer " + jwt.getToken().getTokenValue());
		return headers;
	}

	/**
	 * Get current access token.
	 * 
	 * @return access token.
	 * @throws ShanoirStudiesException
	 */
	private static JwtAuthenticationToken getJwtAuthenticationToken() throws SecurityException {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			throw new SecurityException("Anonymous user");
		}
		return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
	}

	public static boolean isUserAnonymous() {
		return (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
	}

}
