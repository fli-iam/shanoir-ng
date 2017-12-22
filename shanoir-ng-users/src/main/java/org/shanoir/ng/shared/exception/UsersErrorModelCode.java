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
