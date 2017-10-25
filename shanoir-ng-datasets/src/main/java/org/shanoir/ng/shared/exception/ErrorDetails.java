package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.error.FieldErrorMap;

/**
 * Add whatever details you want here
 *
 * @author jlouis
 */
public class ErrorDetails {

	FieldErrorMap fieldErrors = null;


	/**
	 *
	 */
	public ErrorDetails() {
	}

	/**
	 * @param errors
	 */
	public ErrorDetails(FieldErrorMap errors) {
		this.fieldErrors = errors;
	}

	/**
	 * @return the fieldErrors
	 */
	public FieldErrorMap getFieldErrors() {
		return fieldErrors;
	}

	/**
	 * @param formErrors the fieldErrors to set
	 */
	public void setFieldErrors(FieldErrorMap fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

}
