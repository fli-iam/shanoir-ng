package org.shanoir.ng.exception.error;

/**
 * @author msimon
 *
 */
public final class ErrorModelCode {

	/** Bad request */
	public static final Integer BAD_REQUEST = 10;
	
	/** Authentication failed */
	public static final Integer AUTHENTICATION_FAILED = 51;
	
	/** Authentication - bad credentials */
	public static final Integer BAD_CREDENTIALS = 52;
	
	/** Authentication - JWT Token expired */
	public static final Integer JWT_TOKEN_EXPIRED = 53;
	
	/** Login - date expired */
	public static final Integer DATE_EXPIRED = 61;
	
	/** Login - account request not validated */
	public static final Integer ACCOUNT_REQUEST_NOT_VALIDATED = 62;
	
	/** No user found */
	public static final Integer USER_NOT_FOUND = 81;
	
	/** Password doesn't match policy */
	public static final Integer PASSWORD_NOT_CORRECT = 82;
	
	/** No account request for user */
	public static final Integer NO_ACCOUNT_REQUEST = 83;
	
}
