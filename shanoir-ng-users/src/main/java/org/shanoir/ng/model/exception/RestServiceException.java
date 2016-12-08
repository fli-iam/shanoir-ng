package org.shanoir.ng.model.exception;

import org.shanoir.ng.model.error.ErrorModel;

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

	/**
	 * @param errorModel the errorModel to set
	 */
	public void setErrorModel(ErrorModel errorModel) {
		this.errorModel = errorModel;
	}

}
