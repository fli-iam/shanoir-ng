package org.shanoir.ng.utils;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.shanoir.ng.shared.exception.UsersErrorModelCode;
import org.shanoir.ng.shared.exception.ShanoirUsersException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for keycloak.
 * 
 * @author msimon
 *
 */
public final class KeycloakUtils {

	public static final String USER_ID_TOKEN_ATT = "userId";
	
	/**
	 * Private constructor
	 */
	private KeycloakUtils() {
	}
	
	/**
	 * Get created user id from keycloak response.
	 * 
	 * @param response
	 *            keycloak response.
	 * @return user id.
	 */
	public static String getCreatedUserId(Response response) {
		final URI location = response.getLocation();
		if (!response.getStatusInfo().equals(Status.CREATED)) {
			final StatusType statusInfo = response.getStatusInfo();
			throw new WebApplicationException("Create method returned status " + statusInfo.getReasonPhrase()
					+ " (Code: " + statusInfo.getStatusCode() + "); expected status: Created (201)", response);
		}
		if (location == null) {
			return null;
		}
		final String path = location.getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/**
	 * Get current user id from Keycloak token.
	 * 
	 * @return user id.
	 * @throws ShanoirStudiesException
	 */
	public static Long getTokenUserId() throws ShanoirUsersException {
		final KeycloakSecurityContext context = getKeycloakSecurityContext();

		final AccessToken accessToken = context.getToken();
		if (accessToken == null) {
			throw new ShanoirUsersException("Token not found", UsersErrorModelCode.USER_NOT_FOUND);
		}
		
		final Map<String, Object> otherClaims = accessToken.getOtherClaims();
		if (otherClaims.containsKey(USER_ID_TOKEN_ATT)) {
			return Long.valueOf(otherClaims.get(USER_ID_TOKEN_ATT).toString());
		}

		throw new ShanoirUsersException("User id not found in token", UsersErrorModelCode.USER_NOT_FOUND);
	}

	/*
	 * Get current access token.
	 * 
	 * @return access token.
	 * @throws ShanoirStudiesException
	 */
	@SuppressWarnings("rawtypes")
	private static KeycloakSecurityContext getKeycloakSecurityContext() throws ShanoirUsersException {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			throw new ShanoirUsersException("Anonymous user", UsersErrorModelCode.USER_NOT_FOUND);
		}
		final KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		return principal.getKeycloakSecurityContext();
	}

}
