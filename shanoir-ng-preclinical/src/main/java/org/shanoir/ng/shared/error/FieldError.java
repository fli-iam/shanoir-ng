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

package org.shanoir.ng.shared.error;

/**
 * Field error.
 * 
 * @author msimon
 *
 */
public class FieldError {

	private String code;
	private String message;
	private Object givenValue;

	/**
	 * @param code
	 * @param message
	 */
	public FieldError(String code, String message, Object givenValue) {
		super();
		this.code = code;
		this.message = message;
		this.givenValue = givenValue;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the givenValue
	 */
	public Object getGivenValue() {
		return givenValue;
	}

	/**
	 * @param givenValue
	 *            the givenValue to set
	 */
	public void setGivenValue(Object givenValue) {
		this.givenValue = givenValue;
	}
}
