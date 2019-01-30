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

package org.shanoir.ng.scheduling;

import java.util.List;

import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.user.User;
import org.shanoir.ng.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks.
 * 
 * @author msimon
 *
 */
@Component
public class ScheduledTasks {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserService userService;

	/**
	 * Check users expiration date every day at 8am.
	 */
	@Scheduled(cron = "0 0 8 * * ?")
	public void checkExpirationDate() {
		// Get list of users who have to receive first expiration notification
		List<User> usersToNotify = userService.getUsersToReceiveFirstExpirationNotification();
		for (User userToNotify : usersToNotify) {
			userToNotify.setFirstExpirationNotificationSent(true);
			try {
				userService.updateExpirationNotification(userToNotify, true);
				emailService.notifyAccountWillExpire(userToNotify);
			} catch (Exception e) {
				LOG.error("Error to send first expiration notification", e);
			}
		}

		// Get list of users who have to receive second expiration notification
		usersToNotify = userService.getUsersToReceiveSecondExpirationNotification();
		for (User userToNotify : usersToNotify) {
			userToNotify.setSecondExpirationNotificationSent(true);
			try {
				userService.updateExpirationNotification(userToNotify, false);
				emailService.notifyAccountWillExpire(userToNotify);
			} catch (Exception e) {
				LOG.error("Error to send second expiration notification", e);
			}
		}
	}

}
