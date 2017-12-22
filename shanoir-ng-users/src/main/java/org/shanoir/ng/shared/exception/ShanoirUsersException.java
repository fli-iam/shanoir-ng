package org.shanoir.ng.shared.exception;

/**
 * Microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirUsersException extends ShanoirException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -7893357900348198538L;


	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirUsersException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirUsersException(final int errorCode) {
		super(errorCode);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirUsersException(final String message, final int errorCode) {
		super(message, errorCode);
	}

}
