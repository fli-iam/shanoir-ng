package org.shanoir.ng.email;

import org.shanoir.ng.user.User;

/**
 * Email service.
 * 
 * @author msimon
 *
 */
public interface EmailService {

	/**
	 * Send an email to administrators to indicate an account request.
	 * 
	 * @param user
	 *            created user.
	 */
	void notifyAdminAccountRequest(User user);

	/**
	 * Send an email on account creation.
	 * 
	 * @param user
	 *            created user.
	 * @param password
	 *            user password.
	 */
	void notifyNewUser(User user, String password);

	/**
	 * Send an email on account request validation.
	 * 
	 * @param user
	 *            accepted user.
	 */
	void notifyUserAccountRequestAccepted(User user);

	/**
	 * Send an email on extension request validation.
	 * 
	 * @param user
	 *            user.
	 */
	void notifyUserExtensionRequestAccepted(User user);

	/**
	 * Send an email on extension request deny.
	 * 
	 * @param user
	 *            user.
	 */
	void notifyUserExtensionRequestDenied(User user);

}
