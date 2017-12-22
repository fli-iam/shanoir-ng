package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.error.FieldErrorMap;

/**
 * Dataset microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirDatasetsException extends ShanoirException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -1272303994850855360L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirDatasetsException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirDatasetsException(final int errorCode) {
		super(errorCode);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorMap
	 *            error map.
	 */
	public ShanoirDatasetsException(final FieldErrorMap errorMap) {
		super(errorMap);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirDatasetsException(final String message, final int errorCode) {
		super(message, errorCode);
	}

}
