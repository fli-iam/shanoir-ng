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

package org.shanoir.ng.user;

import java.util.Map;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Implementation of service used to manage connected user.
 * 
 * @author msimon
 *
 */
@Service
public class CurrentUserServiceImpl implements CurrentUserService {

	private static final Logger LOG = LoggerFactory.getLogger(CurrentUserServiceImpl.class);

	private static final String USER_ID_TOKEN_ATT = "userId";

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canAccessUser(final Long userId) {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			LOG.warn("anonymous user is connected, access refused to user " + userId);
			return false;
		}
		final KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		final AccessToken accessToken = principal.getKeycloakSecurityContext().getToken();
		if (accessToken == null) {
			return false;
		}

		Long tokenUserId = null;
		final Map<String, Object> otherClaims = accessToken.getOtherClaims();
		if (otherClaims.containsKey(USER_ID_TOKEN_ATT)) {
			tokenUserId = Long.valueOf(otherClaims.get(USER_ID_TOKEN_ATT).toString());
		}

		if (userId.equals(tokenUserId) || accessToken.getRealmAccess().isUserInRole("adminRole")) {
			return true;
		}
		return false;
	}

}
