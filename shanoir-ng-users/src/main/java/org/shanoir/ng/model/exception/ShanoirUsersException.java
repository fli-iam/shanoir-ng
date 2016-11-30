package org.shanoir.ng.model.exception;

import org.slf4j.Logger;

/**
 * Microservice exception.
 * 
 * @author msimon
 *
 */
public class ShanoirUsersException extends Exception {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -1272303994850855360L;
	
	/**
	 * Constructor.
	 * 
	 * @param message
	 */
	public ShanoirUsersException(String message) {
		super(message);
	}
	
	/**
	 * Log error and throw exception
	 * 
	 * @param logger
	 * @param message
	 * @return
	 * @throws ShanoirUsersException
	 */
	public static ShanoirUsersException logAndThrow(Logger logger, String message) throws ShanoirUsersException {
		ShanoirUsersException e = new ShanoirUsersException(message);
        logger.error(message, e);
        throw e;
    }

}
