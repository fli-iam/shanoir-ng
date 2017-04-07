package org.shanoir.ng.shared.exception;

import org.slf4j.Logger;

/**
 * Microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirSubjectException extends Exception {

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
	public ShanoirSubjectException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirSubjectException(final int errorCode) {
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
	public ShanoirSubjectException(final String message, final int errorCode) {
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
	 * @throws ShanoirTemplateException
	 */
	public static void logAndThrow(final Logger logger, final String message) throws ShanoirSubjectException {
		final ShanoirSubjectException e = new ShanoirSubjectException(message);
		logger.error(message, e);
		throw e;
	}

}
