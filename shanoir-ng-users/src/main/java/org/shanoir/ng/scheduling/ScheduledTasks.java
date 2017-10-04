package org.shanoir.ng.scheduling;

import java.util.List;

import org.joda.time.DateTime;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.user.User;
import org.shanoir.ng.user.UserRepository;
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
	private UserRepository userRepository;
	
	/**
	 * Check users expiration date every day at 8am.
	 */
	@Scheduled(cron="0 0 8 * * *")
	public void checkExpirationDate() {
		final DateTime dateTime = new DateTime().withMillisOfDay(0).plusWeeks(1);
		
		// Get list of users who have to receive second expiration notification
		List<User> usersToNotify = userRepository.findByExpirationDateLessThanAndFirstExpirationNotificationSent(dateTime.toDate(), true);
		for (User userToNotify : usersToNotify) {
			userToNotify.setSecondExpirationNotificationSent(true);
			try {
				userRepository.save(userToNotify);
				emailService.notifyAccountWillExpire(userToNotify);
			} catch (Exception e) {
				LOG.error("Error to send second expiration notification", e);
			}
		}
		
		// Get list of users who have to receive first expiration notification
		usersToNotify = userRepository.findByExpirationDateLessThanAndFirstExpirationNotificationSent(dateTime.toDate(), false);
		for (User userToNotify : usersToNotify) {
			userToNotify.setFirstExpirationNotificationSent(true);
			try {
				userRepository.save(userToNotify);
				emailService.notifyAccountWillExpire(userToNotify);
			} catch (Exception e) {
				LOG.error("Error to send first expiration notification", e);
			}
		}
	}
	
}
