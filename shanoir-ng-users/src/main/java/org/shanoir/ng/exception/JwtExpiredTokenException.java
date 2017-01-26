package org.shanoir.ng.exception;

import org.shanoir.ng.configuration.security.jwt.token.JwtToken;
import org.springframework.security.core.AuthenticationException;

/**
 * Exception - JWT token has expired.
 * 
 * @author msimon
 *
 */
public class JwtExpiredTokenException extends AuthenticationException {

	/** UID */
	private static final long serialVersionUID = -8012805737493683114L;

	private JwtToken token;

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            message.
	 */
	public JwtExpiredTokenException(final String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 * 
	 * @param token
	 *            token.
	 * @param msg
	 *            message.
	 * @param t
	 *            parent exception.
	 */
	public JwtExpiredTokenException(final JwtToken token, final String msg, final Throwable t) {
		super(msg, t);
		this.token = token;
	}

	/**
	 * Retrieve token.
	 * 
	 * @return token.
	 */
	public String token() {
		return this.token.getToken();
	}
	
}
