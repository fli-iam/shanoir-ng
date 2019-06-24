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

package org.shanoir.challengeScores.data.model.exception;

import io.swagger.model.ErrorModel;

public class RestServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	private int code;
	private String message;

	/**
	 * @param code
	 */
	public RestServiceException(int code, String message) {
		super();
		this.code = code;
		this.message = message;
	}


	public ErrorModel toErrorModel() {
		return new ErrorModel().code(code).message(message);
	}


	/**
	 * @param message the message to set
	 */
	protected void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return super.toString() + ". " + message;
	}


	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
}
