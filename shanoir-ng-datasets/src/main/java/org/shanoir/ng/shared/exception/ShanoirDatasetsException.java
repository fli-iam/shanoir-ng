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
 * Dataset microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirDatasetsException extends ShanoirException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -1272303994850855360L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public ShanoirDatasetsException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirDatasetsException(final int errorCode) {
		super(errorCode);
	}

	/**
	 * Constructor.
	 * 
	 * @param errorMap
	 *            error map.
	 */
	public ShanoirDatasetsException(final FieldErrorMap errorMap) {
		super(errorMap);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirDatasetsException(final String message, final int errorCode) {
		super(message, errorCode);
	}

}
