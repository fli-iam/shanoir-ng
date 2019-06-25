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

import org.springframework.security.core.AuthenticationException;

/**
 * @author msimon
 *
 */
public class ShanoirAuthenticationException extends AuthenticationException {

	/** UID */
	private static final long serialVersionUID = 7841809673349916686L;

	private int errorCode;

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            message.
	 */
	public ShanoirAuthenticationException(final String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            message.
	 * @param t
	 *            parent exception.
	 */
	public ShanoirAuthenticationException(final String msg, final Throwable t) {
		super(msg, t);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 * @param errorCode
	 *            error code.
	 */
	public ShanoirAuthenticationException(final String message, final int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

}
