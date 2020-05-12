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

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class SecurityContextTestUtil {
	/**
	 * Set a @Principal in authentication context.
	 */
	public static void initAuthenticationContext() {
		Access realmAccess = new Access();
		realmAccess.addRole("ROLE_ADMIN");
		AccessToken accessToken = new AccessToken();
		accessToken.setRealmAccess(realmAccess);
		KeycloakSecurityContext context = new KeycloakSecurityContext(null, accessToken, null, null);
		KeycloakPrincipal<KeycloakSecurityContext> principal = new KeycloakPrincipal<>("test", context);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
	}
}
