package org.shanoir.ng.utils;

import java.util.Map;
import java.util.Set;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for Keycloak requests.
 * 
 * @author msimon
 *
 */
public final class KeycloakUtil {

	private static final String USER_ID_TOKEN_ATT = "userId";

	/**
	 * Get current user roles from Keycloak token.
	 * 
	 * @return user roles.
	 * @throws ShanoirStudiesException
	 */
	public static Set<String> getTokenRoles() throws ShanoirException {
		final KeycloakSecurityContext context = getKeycloakSecurityContext();

		final AccessToken accessToken = context.getToken();
		if (accessToken == null) {
			throw new ShanoirException("Token not found");
		}
		
		return accessToken.getRealmAccess().getRoles();
	}

	/**
	 * Get current user id from Keycloak token.
	 * 
	 * @return user id.
	 * @throws ShanoirStudiesException
	 */
	public static Long getTokenUserId() throws ShanoirException {
		final KeycloakSecurityContext context = getKeycloakSecurityContext();

		final AccessToken accessToken = context.getToken();
		if (accessToken == null) {
			throw new ShanoirException("Token not found");
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
	public static HttpHeaders getKeycloakHeader() throws ShanoirException {
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
	private static KeycloakSecurityContext getKeycloakSecurityContext() throws ShanoirException {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			throw new ShanoirException("Anonymous user");
		}
		final KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		return principal.getKeycloakSecurityContext();
	}

}
