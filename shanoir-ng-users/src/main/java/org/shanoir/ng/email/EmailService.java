package org.shanoir.ng.email;

import org.shanoir.ng.user.model.User;

/**
 * Email service.
 * 
 * @author msimon
 *
 */
public interface EmailService {

	/**
	 * Send an email to user if account will expire soon.
	 * 
	 * @param user
	 *            user.
	 */
	void notifyAccountWillExpire(User user);

	/**
	 * Send an email to administrators to indicate an account request.
	 * 
	 * @param user
	 *            created user.
	 */
	void notifyAdminAccountRequest(User user);

	/**
	 * Send an email to the user and all administrators on account request validation.
	 * 
	 * @param user
	 *            accepted user.
	 */
	void notifyAccountRequestAccepted(User user);

	/**
	 * Send an email to the user and all administrators on account request validation.
	 * 
	 * @param user
	 *            accepted user.
	 */
	void notifyAccountRequestDenied(User user);

	/**
	 * Send an email to the user and all administrators on extension request validation.
	 * 
	 * @param user
	 *            user.
	 */
	void notifyExtensionRequestAccepted(User user);

	/**
	 * Send an email to the user and all administrators on extension request deny.
	 * 
	 * @param user
	 *            user.
	 */
	void notifyExtensionRequestDenied(User user);

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
	 * Send an email on user password reset.
	 * 
	 * @param user
	 *            user.
	 * @param password
	 *            new password.
	 */
	void notifyUserResetPassword(User user, String password);

}
