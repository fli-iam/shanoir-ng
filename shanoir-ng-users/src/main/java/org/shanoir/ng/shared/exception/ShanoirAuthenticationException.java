package org.shanoir.ng.shared.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author msimon
 *
 */
public class ShanoirAuthenticationException extends AuthenticationException {

	/** UID */
	private static final long serialVersionUID = 7841809673349916686L;

	private int errorCode;

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            message.
	 */
	public ShanoirAuthenticationException(final String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            message.
	 * @param t
	 *            parent exception.
	 */
	public ShanoirAuthenticationException(final String msg, final Throwable t) {
		super(msg, t);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirAuthenticationException(final String message, final int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

}
