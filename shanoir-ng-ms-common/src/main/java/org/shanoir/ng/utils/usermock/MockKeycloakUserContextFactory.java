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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.StringUtils;

public class MockKeycloakUserContextFactory implements WithSecurityContextFactory<WithMockKeycloakUser> {

	public SecurityContext createSecurityContext(WithMockKeycloakUser withUser) {
		String username = StringUtils.hasLength(withUser.username()) ? withUser.username() : withUser.value();
		if (username == null) {
			throw new IllegalArgumentException(
					withUser + " cannot have null username on both username and value properties");
		}

		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		for (String authority : withUser.authorities()) {
			grantedAuthorities.add(new SimpleGrantedAuthority(authority));
		}

		if (grantedAuthorities.isEmpty()) {
			for (String role : withUser.roles()) {
				if (role.startsWith("ROLE_")) {
					throw new IllegalArgumentException("roles cannot start with ROLE_ Got " + role);
				}
				grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
			}
		} else if (!(withUser.roles().length == 1 && "USER".equals(withUser.roles()[0]))) {
			throw new IllegalStateException("You cannot define roles attribute " + Arrays.asList(withUser.roles())
					+ " with authorities attribute " + Arrays.asList(withUser.authorities()));
		}
		Map<String, Object> claims = Map.of("preferred_username", username, "userId", withUser.id(), "realm_access", grantedAuthorities);
		Jwt jwt = new Jwt("mock-token-value", Instant.now(), Instant.now().plusSeconds(300), Map.of("header", "mock"), claims);
		Authentication authentication = new JwtAuthenticationToken(jwt, grantedAuthorities);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		return context;
	}

}
