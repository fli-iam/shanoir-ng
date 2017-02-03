package org.shanoir.ng.configuration.security.jwt;

import java.util.List;
import java.util.stream.Collectors;

import org.shanoir.ng.configuration.security.jwt.token.AccessJwtToken;
import org.shanoir.ng.configuration.security.jwt.token.JwtAuthenticationToken;
import org.shanoir.ng.configuration.security.jwt.token.JwtSettings;
import org.shanoir.ng.model.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * JWT authentication provider.
 * 
 * @author msimon
 *
 */
//@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private JwtSettings jwtSettings;

	@SuppressWarnings("unchecked")
	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
		// Get access token
		final AccessJwtToken accessToken = (AccessJwtToken) authentication.getCredentials();

		// Get claims
		final Jws<Claims> jwsClaims = accessToken.parseClaims(jwtSettings.getTokenSigningKey());
		final String subject = jwsClaims.getBody().getSubject();
		final Integer userId = jwsClaims.getBody().get(JwtSettings.USER_ID, Integer.class);
		final List<String> scopes = jwsClaims.getBody().get(JwtSettings.SCOPES, List.class);
		final List<GrantedAuthority> authorities = scopes.stream()
				.map(authority -> new SimpleGrantedAuthority(authority)).collect(Collectors.toList());

		final UserContext user = new UserContext();
		if (userId != null) {
			user.setId(Long.valueOf(userId));
		}
		user.setUsername(subject);
		user.setAuthorities(authorities);

		return new JwtAuthenticationToken(user, user.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}

}
