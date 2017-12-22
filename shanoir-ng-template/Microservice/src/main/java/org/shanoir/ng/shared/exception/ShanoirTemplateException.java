package org.shanoir.ng.shared.exception;

/**
 * Microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirTemplateException extends ShanoirException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -3239198004067077517L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirTemplateException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirTemplateException(final int errorCode) {
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
	public ShanoirTemplateException(final String message, final int errorCode) {
		super(message, errorCode);
	}

}
