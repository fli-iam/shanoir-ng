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
