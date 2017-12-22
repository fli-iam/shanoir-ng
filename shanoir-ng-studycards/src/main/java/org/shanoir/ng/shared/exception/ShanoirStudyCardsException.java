package org.shanoir.ng.shared.exception;

/**
 * Study card microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirStudyCardsException extends ShanoirException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 6234542575489938332L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirStudyCardsException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirStudyCardsException(final int errorCode) {
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
	public ShanoirStudyCardsException(final String message, final int errorCode) {
		super(message, errorCode);
	}

}
