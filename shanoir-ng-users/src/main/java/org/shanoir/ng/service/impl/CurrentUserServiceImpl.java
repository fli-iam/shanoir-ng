package org.shanoir.ng.service.impl;

import java.util.Map;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.shanoir.ng.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
		
		Long tokenUserId = null;
		final Map<String, Object> otherClaims = accessToken.getOtherClaims();
	    if (otherClaims.containsKey(USER_ID_TOKEN_ATT)) {
	    	tokenUserId = Long.valueOf(otherClaims.get(USER_ID_TOKEN_ATT).toString());
	    }

		if (accessToken != null
				&& ((userId.equals(tokenUserId) || accessToken.getRealmAccess().isUserInRole("ADMIN")))) {
			return true;
		}
		return false;
	}

}
