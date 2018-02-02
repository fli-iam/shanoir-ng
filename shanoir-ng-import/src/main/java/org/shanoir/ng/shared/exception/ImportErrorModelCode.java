package org.shanoir.ng.shared.exception;

/**
 * List of error codes for current microservice.
 * 
 * @author msimon
 *
 */
public class ImportErrorModelCode extends ErrorModelCode {
	
	/** User has no right to perform an action */
	public static final Integer NO_RIGHT_FOR_ACTION = 11;

	/** Login - bad credentials */
	public static final Integer SC_MS_COMM_FAILURE = 51;
	
}
