package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.slf4j.Logger;

/**
 * Microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirDatasetException extends Exception {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -1272303994850855360L;

	private int errorCode;

	private FieldErrorMap errorMap;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirDatasetException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirDatasetException(final int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	/**
	 * Constructor.
	 * 
	 * @param errorMap
	 *            error map.
	 */
	public ShanoirDatasetException(final FieldErrorMap errorMap) {
		super();
		this.errorMap = errorMap;
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirDatasetException(final String message, final int errorCode) {
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
	 * @return the errorMap
	 */
	public FieldErrorMap getErrorMap() {
		return errorMap;
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
	public static void logAndThrow(final Logger logger, final String message) throws ShanoirDatasetException {
		final ShanoirDatasetException e = new ShanoirDatasetException(message);
		logger.error(message, e);
		throw e;
	}

}
