package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.error.FieldErrorMap;

/**
 * Microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirException extends Exception {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -127890367512961204L;

	private int errorCode;

	private FieldErrorMap errorMap;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirException(final int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	/**
	 * Constructor.
	 * 
	 * @param errorMap
	 *            error map.
	 */
	public ShanoirException(final FieldErrorMap errorMap) {
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
	public ShanoirException(final String message, final int errorCode) {
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

}
