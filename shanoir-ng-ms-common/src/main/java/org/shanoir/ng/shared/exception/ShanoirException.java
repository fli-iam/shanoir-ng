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

import org.shanoir.ng.shared.error.FieldErrorMap;

/**
 * Microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirException extends Exception {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -127890367512961204L;

	private int errorCode;

	private FieldErrorMap errorMap;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirException(final String message, Throwable cause) {
		super(message, cause);
	}
	
	public ShanoirException(final String message) {
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param cause
	 * @param code
	 */
	public ShanoirException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor
	 * 
	 * @param cause
	 * @param code
	 * @param errorCode
	 */
	public ShanoirException(final String message, final Throwable cause, final int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirException(final int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	/**
	 * Constructor.
	 * 
	 * @param errorMap
	 *            error map.
	 */
	public ShanoirException(final FieldErrorMap errorMap) {
		super();
		this.errorMap = errorMap;
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirException(final String message, final int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * @return the errorMap
	 */
	public FieldErrorMap getErrorMap() {
		return errorMap;
	}

}
