package org.shanoir.ng.model.error;

import java.util.List;

/**
 * Add whatever details you want here
 *
 * @author jlouis
 */
public class ErrorDetails {

	List<FieldError> fieldErrors = null;


	/**
	 *
	 */
	public ErrorDetails() {
	}

	/**
	 * @param fieldErrors
	 */
	public ErrorDetails(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	/**
	 * @return the fieldErrors
	 */
	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}

	/**
	 * @param formErrors the fieldErrors to set
	 */
	public void setFieldErrors(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

}
