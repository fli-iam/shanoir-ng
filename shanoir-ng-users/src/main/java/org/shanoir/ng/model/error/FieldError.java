package org.shanoir.ng.model.error;

import java.util.ArrayList;
import java.util.List;

public class FieldError {

	private String fieldName;

	private List<String> errorCodes = new ArrayList<String>();

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 * @param errorCodes
	 */
	public FieldError(String fieldName, List<String> errorCodes) {
		super();
		this.fieldName = fieldName;
		this.errorCodes = errorCodes;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return the errorCodes
	 */
	public List<String> getErrorCodes() {
		return errorCodes;
	}

	/**
	 * @param errorCodes the errorCodes to set
	 */
	public void setErrorCodes(List<String> errorCodes) {
		this.errorCodes = errorCodes;
	}



}
