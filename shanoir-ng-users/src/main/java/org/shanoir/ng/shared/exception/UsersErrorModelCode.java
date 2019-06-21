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
 * List of error codes for current microservice.
 * 
 * @author msimon
 *
 */
public class UsersErrorModelCode extends ErrorModelCode {

	/** No user found */
	public static final Integer USER_NOT_FOUND = 81;

	/** Password doesn't match policy */
	public static final Integer PASSWORD_NOT_CORRECT = 82;

	/** No account request for user */
	public static final Integer NO_ACCOUNT_REQUEST = 83;

}
