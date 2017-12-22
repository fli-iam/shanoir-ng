package org.shanoir.ng.shared.exception;

/**
 * Import microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirImportException extends ShanoirException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 4867137215681474376L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirImportException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirImportException(final int errorCode) {
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
	public ShanoirImportException(final String message, final int errorCode) {
		super(message, errorCode);
	}

}
