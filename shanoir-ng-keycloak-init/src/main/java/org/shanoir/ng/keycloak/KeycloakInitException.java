package org.shanoir.ng.keycloak;

/**
 * Custom exception
 * 
 * @author msimon
 *
 */
public class KeycloakInitException extends Exception {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 8711724661528838071L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            exception message.
	 */
	public KeycloakInitException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            exception message.
	 * @param cause
	 *            exception cause.
	 */
	public KeycloakInitException(String message, Throwable cause) {
		super(message, cause);
	}
}
