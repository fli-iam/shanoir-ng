package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.error.FieldErrorMap;

/**
 * Study microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirStudiesException extends ShanoirException {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 614665107614340916L;

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
		super(errorCode);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorMap
	 *            error map.
	 */
	public ShanoirStudiesException(final FieldErrorMap errorMap) {
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
	public ShanoirStudiesException(final String message, final int errorCode) {
		super(message, errorCode);
	}

}
