package org.shanoir.ng.exception;

import org.slf4j.Logger;

/**
 * Microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirUsersException extends Exception {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -1272303994850855360L;

	private int errorCode;

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
		super();
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Log error and throw exception
	 * 
	 * @param logger
	 *            logger.
	 * @param message
	 *            message.
	 * @throws ShanoirUsersException
	 */
	public static void logAndThrow(final Logger logger, final String message) throws ShanoirUsersException {
		ShanoirUsersException e = new ShanoirUsersException(message);
		logger.error(message, e);
		throw e;
	}

}
