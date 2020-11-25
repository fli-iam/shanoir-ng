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
import java.util.List;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for security context.
 * 
 * @author msimon, jlouis
 *
 */
public abstract class SecurityContextUtil {
	
	/**
	 * Clear the authentication
	 */
	public static void clearAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

	/**
	 * Set a @Principal in authentication context.
	 * 
	 * @param role "ROLE_ADMIN" or "ROLE_EXPERT" or ...
	 */
	public static void initAuthenticationContext(String role) {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

		GrantedAuthority auth = new SimpleGrantedAuthority("ROLE_ADMIN");
		grantedAuthorities.add(auth );
		Access realmAccess = new Access();
		realmAccess.addRole(role);
		AccessToken accessToken = new AccessToken();
		accessToken.getOtherClaims().put(KeycloakUtil.USER_ID_TOKEN_ATT, 1);
		accessToken.setRealmAccess(realmAccess);
		KeycloakSecurityContext context = new KeycloakSecurityContext(null, accessToken, null, null);
		KeycloakPrincipal<KeycloakSecurityContext> principal = new KeycloakPrincipal<>("rabbitMQ", context);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, "password", grantedAuthorities));
	}

}
