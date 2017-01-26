package org.shanoir.ng.configuration.security.jwt.token;

import java.util.List;
import java.util.Optional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * JWT refresh token.
 * 
 * @author msimon
 *
 */
public class RefreshToken implements JwtToken {

	private Jws<Claims> claims;

	private RefreshToken(final Jws<Claims> claims) {
		this.claims = claims;
	}

	/**
	 * Creates and validates refresh token.
	 * 
	 * @param token
	 *            access token.
	 * @param signingKey
	 *            signing key.
	 * @return refresh token.
	 */
	@SuppressWarnings("unchecked")
	public static Optional<RefreshToken> create(final AccessJwtToken token, final String signingKey) {
		final Jws<Claims> claims = token.parseClaims(signingKey);

		final List<String> scopes = claims.getBody().get(JwtSettings.SCOPES, List.class);
		if (scopes == null || scopes.isEmpty()
				|| !scopes.stream().filter(scope -> JwtSettings.REFRESH_TOKEN.equals(scope)).findFirst().isPresent()) {
			return Optional.empty();
		}

		return Optional.of(new RefreshToken(claims));
	}

	@Override
	public String getToken() {
		return null;
	}

	/**
	 * Get claims.
	 * 
	 * @return claims.
	 */
	public Jws<Claims> getClaims() {
		return claims;
	}

	/**
	 * Get token JTI.
	 * 
	 * @return JTI
	 */
	public String getJti() {
		return claims.getBody().getId();
	}

	/**
	 * Get token subject.
	 * 
	 * @return subject.
	 */
	public String getSubject() {
		return claims.getBody().getSubject();
	}

}
