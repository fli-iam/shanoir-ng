package org.shanoir.ng.utils;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
		Access realmAccess = new Access();
		realmAccess.addRole(role);
		AccessToken accessToken = new AccessToken();
		accessToken.setRealmAccess(realmAccess);
		KeycloakSecurityContext context = new KeycloakSecurityContext(null, accessToken, null, null);
		KeycloakPrincipal<KeycloakSecurityContext> principal = new KeycloakPrincipal<>("user", context);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
	}

}
