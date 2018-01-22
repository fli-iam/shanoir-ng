package org.shanoir.ng.utils;

import java.net.URI;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

/**
 * Utility class for keycloak.
 * 
 * @author msimon
 *
 */
public final class KeycloakShanoirUtil {

	/**
	 * Private constructor
	 */
	private KeycloakShanoirUtil() {
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

}
