package org.shanoir.ng.configuration.security.jwt.token;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.model.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT token factory.
 * 
 * @author msimon
 *
 */
@Component
public class JwtTokenFactory {

	@Autowired
	private JwtSettings settings;

	/**
	 * Factory method for issuing JWT access tokens.
	 * 
	 * @param userContext
	 *            user context.
	 * @return access token.
	 */
	public AccessJwtToken createAccessJwtToken(final UserContext userContext) {
		if (StringUtils.isBlank(userContext.getUsername())) {
			throw new IllegalArgumentException("Cannot create JWT Token without username");
		}

		if (userContext.getAuthorities() == null || userContext.getAuthorities().isEmpty()) {
			throw new IllegalArgumentException("User doesn't have any privileges");
		}

		final Claims claims = Jwts.claims().setSubject(userContext.getUsername());
		claims.put(JwtSettings.USER_ID, userContext.getId());
		claims.put(JwtSettings.SCOPES,
				userContext.getAuthorities().stream().map(s -> s.getAuthority()).collect(Collectors.toList()));

		final DateTime currentTime = new DateTime();

		final String token = Jwts.builder().setClaims(claims).setIssuer(settings.getTokenIssuer())
				.setIssuedAt(currentTime.toDate())
				.setExpiration(currentTime.plusMinutes(settings.getTokenExpirationTime()).toDate())
				.signWith(SignatureAlgorithm.HS512, settings.getTokenSigningKey()).compact();

		return new AccessJwtToken(token);
	}

	/**
	 * Factory method for issuing JWT refresh tokens.
	 * 
	 * @param userContext
	 *            user context.
	 * @return token.
	 */
	public JwtToken createRefreshToken(final UserContext userContext) {
		if (StringUtils.isBlank(userContext.getUsername())) {
			throw new IllegalArgumentException("Cannot create JWT Token without username");
		}

		final DateTime currentTime = new DateTime();

		final Claims claims = Jwts.claims().setSubject(userContext.getUsername());
		claims.put(JwtSettings.USER_ID, userContext.getId());
		claims.put(JwtSettings.SCOPES, Arrays.asList(JwtSettings.REFRESH_TOKEN));

		final String token = Jwts.builder().setClaims(claims).setIssuer(settings.getTokenIssuer())
				.setId(UUID.randomUUID().toString()).setIssuedAt(currentTime.toDate())
				.setExpiration(currentTime.plusMinutes(settings.getRefreshTokenExpTime()).toDate())
				.signWith(SignatureAlgorithm.HS512, settings.getTokenSigningKey()).compact();

		return new AccessJwtToken(token, claims);
	}

}
