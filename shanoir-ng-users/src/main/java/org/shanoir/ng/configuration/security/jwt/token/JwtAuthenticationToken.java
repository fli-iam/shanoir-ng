package org.shanoir.ng.configuration.security.jwt.token;

import java.util.Collection;

import org.shanoir.ng.model.auth.UserContext;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * JWT authentication token.
 * 
 * @author msimon
 *
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	/** UID */
	private static final long serialVersionUID = -342402662993974393L;

	private AccessJwtToken accessToken;
	private UserContext userContext;

	/**
	 * Constructor
	 * 
	 * @param unsafeToken
	 *            access token.
	 */
	public JwtAuthenticationToken(final AccessJwtToken unsafeToken) {
		super(null);
		this.accessToken = unsafeToken;
		this.setAuthenticated(false);
	}

	/**
	 * Constructor
	 * 
	 * @param userContext
	 *            user context.
	 * @param authorities
	 *            user authorities.
	 */
	public JwtAuthenticationToken(final UserContext userContext,
			final Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.eraseCredentials();
		this.userContext = userContext;
		super.setAuthenticated(true);
	}

	@Override
	public void eraseCredentials() {
		super.eraseCredentials();
		accessToken = null;
	}

	@Override
	public Object getCredentials() {
		return accessToken;
	}

	@Override
	public Object getPrincipal() {
		return userContext;
	}

	@Override
	public void setAuthenticated(final boolean authenticated) {
		if (authenticated) {
			throw new IllegalArgumentException(
					"Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
		}
		super.setAuthenticated(authenticated);
	}

}
