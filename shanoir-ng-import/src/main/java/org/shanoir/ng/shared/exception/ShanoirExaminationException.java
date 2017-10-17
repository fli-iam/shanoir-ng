package org.shanoir.ng.shared.exception;

import org.slf4j.Logger;

/**
 * Microservice exception.
 * 
 * @author ifakhfakh
 *
 */
public class ShanoirExaminationException extends Exception {

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
	public ShanoirExaminationException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirExaminationException(final int errorCode) {
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
	public ShanoirExaminationException(final String message, final int errorCode) {
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
	 * @throws ShanoirExaminationException
	 */
	public static void logAndThrow(final Logger logger, final String message) throws ShanoirExaminationException {
		final ShanoirExaminationException e = new ShanoirExaminationException(message);
		logger.error(message, e);
		throw e;
	}

}
