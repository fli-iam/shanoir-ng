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

package org.shanoir.ng.utils.usermock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.StringUtils;

public class MockKeycloakUserContextFactory implements WithSecurityContextFactory<WithMockKeycloakUser> {
	
    public SecurityContext createSecurityContext(WithMockKeycloakUser withUser) {
    	String username = StringUtils.hasLength(withUser.username()) ? withUser
				.username() : withUser.value();
		if (username == null) {
			throw new IllegalArgumentException(withUser
					+ " cannot have null username on both username and value properites");
		}

		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		for (String authority : withUser.authorities()) {
			grantedAuthorities.add(new SimpleGrantedAuthority(authority));
		}

		if(grantedAuthorities.isEmpty()) {
			for (String role : withUser.roles()) {
				if (role.startsWith("ROLE_")) {
					throw new IllegalArgumentException("roles cannot start with ROLE_ Got " + role);
				}
				grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
			}
		} 
		else if(!(withUser.roles().length == 1 && "USER".equals(withUser.roles()[0]))) {
			throw new IllegalStateException("You cannot define roles attribute "+ Arrays.asList(withUser.roles())
				+" with authorities attribute "+ Arrays.asList(withUser.authorities()));
		}

		@SuppressWarnings("rawtypes")
		KeycloakPrincipal principal = mockKeycloakPrincipal(withUser.id(), username, withUser.authorities());
		
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal, withUser.password(), grantedAuthorities);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		return context;
    }
    
    private KeycloakPrincipal<KeycloakSecurityContext> mockKeycloakPrincipal(long id, String username, String[] roles) {    	
    	Access realmAccess = new Access();
		for (String role : roles) {
			realmAccess.addRole(role);
		}
		AccessToken accessToken = new AccessToken();
		accessToken.getOtherClaims().put(KeycloakUtil.USER_ID_TOKEN_ATT, id);
		accessToken.setRealmAccess(realmAccess);
		KeycloakSecurityContext context = new KeycloakSecurityContext(null, accessToken, null, null);
		KeycloakPrincipal<KeycloakSecurityContext> principal = new KeycloakPrincipal<>(username, context);
		return principal;
    }
}


