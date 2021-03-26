/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
	 * @param cause
	 * @param code
	 */
	public RestServiceException(Throwable cause, ErrorModel errorModel) {
		super(cause);
		this.errorModel = errorModel;
	}

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
