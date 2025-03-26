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

package org.shanoir.ng.user.security;

import java.util.Map;

import org.shanoir.ng.shared.exception.TokenNotFoundException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class IsMeSecurityService {
		
    public boolean isMe(Long id) {
    	if (id == null) throw new IllegalArgumentException("id cannot be null");
    	else if (isAnonymousConnected()) return false;
    	else return id.equals(getConnectedUserId());
    }

    public boolean isMe(String username) {
    	final JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		return username.equals(authentication.getToken().getClaimAsString("preferred_username"));
    }

    public boolean isMe(User user) {
    	if (user == null) throw new IllegalArgumentException("user cannot be null");
    	else return isMe(user.getId());
    }

    private boolean isAnonymousConnected() {
    	return SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken;
    }

    private Long getConnectedUserId() {
    	final JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

    	final Jwt jwt = authentication.getToken();
		if (jwt == null) {
			throw new TokenNotFoundException("Cannot find token while checking access to method");
		}

		Long tokenUserId = null;
		final Map<String, Object> otherClaims = jwt.getClaims();
		if (otherClaims.containsKey(KeycloakUtil.USER_ID_TOKEN_ATT)) {
			tokenUserId = Long.valueOf(otherClaims.get(KeycloakUtil.USER_ID_TOKEN_ATT).toString());
		}

		return tokenUserId;
    }

}