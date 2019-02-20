package org.shanoir.ng.shared.exception;

/**
 * Microservice exception.
 * 
 * @author jlouis
 *
 */
public class AccountNotOnDemandException extends ShanoirUsersException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3642967155110660124L;

	/**
	 * Constructor.
	 * 
	 * @param userId user id.
	 */
	public AccountNotOnDemandException(Long userId) {
		super("The user with id " + userId + " is not 'on demand'");
	}

}
