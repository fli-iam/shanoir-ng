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
