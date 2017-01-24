package org.shanoir.ng.configuration.security.jwt.token;

import org.shanoir.ng.exception.JwtExpiredTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

/**
 * JWT access token.
 * 
 * @author msimon
 *
 */
public class AccessJwtToken implements JwtToken {

	private static Logger logger = LoggerFactory.getLogger(AccessJwtToken.class);

	private String token;

	@JsonIgnore
	private Claims claims;

	public AccessJwtToken(final String token) {
		this.token = token;
	}

	protected AccessJwtToken(final String token, final Claims claims) {
		this.token = token;
		this.claims = claims;
	}

	/**
	 * Parses and validates JWT Token signature.
	 * 
	 * @param signingKey
	 *            signing key.
	 * @throws BadCredentialsException
	 * @throws JwtExpiredTokenException
	 * 
	 */
	public Jws<Claims> parseClaims(final String signingKey) {
		try {
			return Jwts.parser().setSigningKey(signingKey).parseClaimsJws(this.token);
		} catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException ex) {
			logger.error("Invalid JWT Token", ex);
			throw new BadCredentialsException("Invalid JWT token: ", ex);
		} catch (ExpiredJwtException expiredEx) {
			logger.info("JWT Token is expired", expiredEx);
			throw new JwtExpiredTokenException(this, "JWT Token expired", expiredEx);
		}
	}

	@Override
	public String getToken() {
		return token;
	}

}
