package org.shanoir.ng.shared.exception;

/**
 * REST service exception.
 * 
 * @author msimon
 *
 */
public class RestServiceException extends Exception {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 2796153429277618391L;

	private ErrorModel errorModel;

	/**
	 * @param code
	 */
	public RestServiceException(ErrorModel errorModel) {
		super();
		this.errorModel = errorModel;
	}

	/**
	 * @return the errorModel
	 */
	public ErrorModel getErrorModel() {
		return errorModel;
	}

}
