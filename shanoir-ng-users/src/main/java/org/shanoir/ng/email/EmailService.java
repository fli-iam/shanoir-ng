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
