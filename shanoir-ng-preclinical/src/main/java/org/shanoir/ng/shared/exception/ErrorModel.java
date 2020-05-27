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

/**
 * ErrorModel
 * 
 * @author jlouis
 */
public class ErrorModel {

	private Integer code;

	private String message;

	private Object details;

	public ErrorModel code(Integer code) {
		this.code = code;
		return this;
	}

	/**
	 * Constructor.
	 * 
	 * @param code
	 */
	public ErrorModel(Integer code) {
		super();
		this.code = code;
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 */
	public ErrorModel message(String message) {
		this.message = message;
		return this;
	}

	/**
	 * Constructor.
	 * 
	 * @param code
	 * @param message
	 */
	public ErrorModel(Integer code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

	/**
	 * Constructor.
	 * 
	 * @param code
	 * @param message
	 * @param details
	 */
	public ErrorModel(Integer code, String message, Object details) {
		super();
		this.code = code;
		this.message = message;
		this.details = details;
	}

	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the details
	 */
	public Object getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(Object details) {
		this.details = details;
	}

}
