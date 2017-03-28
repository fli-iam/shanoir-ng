package org.shanoir.ng.shared.exception;

import org.slf4j.Logger;

public class ShanoirStudiesException extends Exception{
	
	private int errorCode;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirStudiesException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirStudiesException(final int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirStudiesException(final String message, final int errorCode) {
		super(message);
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
	public static void logAndThrow(final Logger logger, final String message) throws ShanoirStudiesException {
		final ShanoirStudiesException e = new ShanoirStudiesException(message);
		logger.error(message, e);
		throw e;
	}


}
