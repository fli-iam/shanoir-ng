package org.shanoir.ng.shared.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(errorModel);
		} catch (JsonProcessingException e) {
			return "error while serializing errorModel : " + e.toString();
		}
	}

}
