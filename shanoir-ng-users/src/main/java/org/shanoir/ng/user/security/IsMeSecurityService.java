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

import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.shanoir.ng.shared.exception.TokenNotFoundException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class IsMeSecurityService {
		
    public boolean isMe(Long id) {
    	if (id == null) throw new IllegalArgumentException("id cannot be null");
    	else if (isAnonymousConnected()) return false;
    	else return id.equals(getConnectedUserId());
    }
    
    @SuppressWarnings("rawtypes")
	public boolean isMe(String username) {
    	final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof KeycloakPrincipal) {
			return username.equals(((KeycloakPrincipal) principal).getName());
		}
		return false;
    }
    
    public boolean isMe(User user) {
    	if (user == null) throw new IllegalArgumentException("user cannot be null");
    	else return isMe(user.getId());
    }
    
    
    
    private boolean isAnonymousConnected() {
    	return SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken;
    }
    
    private Long getConnectedUserId() {
    	if (!(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof KeycloakPrincipal)) {
    		throw new IllegalArgumentException("Cannot get the connected user id because principal is not an instance of KeycloakPrincipal. "
    				+ "If this error occures in a unit test context, maybe user @WithMockKeycloakUser instead of @WithMockUser.");
    	}
		@SuppressWarnings("rawtypes")
		final KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		final AccessToken accessToken = principal.getKeycloakSecurityContext().getToken();
		if (accessToken == null) {
			throw new TokenNotFoundException("Cannot find token while checking access to method");
		}

		Long tokenUserId = null;
		final Map<String, Object> otherClaims = accessToken.getOtherClaims();
		if (otherClaims.containsKey(KeycloakUtil.USER_ID_TOKEN_ATT)) {
			tokenUserId = Long.valueOf(otherClaims.get(KeycloakUtil.USER_ID_TOKEN_ATT).toString());
		}

		return tokenUserId;
    }
 
    
}