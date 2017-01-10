package org.shanoir.ng.exception.error;

/**
 * @author msimon
 *
 */
public final class ErrorModelCode {

	/** Password doesn't match policy */
	public static final Integer PASSWORD_NOT_CORRECT = 10;
	
	/** Login - bad credentials */
	public static final Integer BAD_CREDENTIALS = 101;
	
	/** Login - date expired */
	public static final Integer DATE_EXPIRED = 102;
	
	/** No user found */
	public static final Integer USER_NOT_FOUND = 151;
	
	/** No account request for user */
	public static final Integer NO_ACCOUNT_REQUEST = 152;
	
}
