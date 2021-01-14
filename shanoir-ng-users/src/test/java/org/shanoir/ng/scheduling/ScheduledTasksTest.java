package org.shanoir.ng.scheduling;

import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test class for ShcheduledTasks class
 * @author fli
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ScheduledTasksTest {


	@MockBean
	private EmailService emailService;

	@MockBean
	private UserService userService;

	@MockBean
	KeycloakClient keycloakClient;

	@Autowired
	private ScheduledTasks scheduledTasks;

	@Test
	public void testCheckExpirationDateExact() {
		// GIVEN three user with near expiration dates
		User user3 = new User();
		user3.setExpirationDate(LocalDate.now());

		given(userService.getUsersToReceiveFirstExpirationNotification()).willReturn(Collections.emptyList());
		given(userService.getUsersToReceiveSecondExpirationNotification()).willReturn(Collections.emptyList());
		given(userService.getExpiredUsers()).willReturn(Collections.singletonList(user3));

		// WHEN we check expiration dates
		scheduledTasks.checkExpirationDate();
		
		// THEN user3 is updated in keycloak database
		Mockito.verify(keycloakClient).updateUser(user3);
	}

	@Test
	public void testCheckExpirationDateFirst() {
		// GIVEN three user with near expiration dates
		User user1 = new User();
		user1.setExpirationDate(LocalDate.now().minusWeeks(1));

		given(userService.getUsersToReceiveFirstExpirationNotification()).willReturn(Collections.singletonList(user1));
		given(userService.getUsersToReceiveSecondExpirationNotification()).willReturn(Collections.emptyList());
		given(userService.getExpiredUsers()).willReturn(Collections.emptyList());

		// WHEN we check expiration dates
		scheduledTasks.checkExpirationDate();
		
		// THEN first expiration warning is sent to user1
		Mockito.verify(userService).updateExpirationNotification(user1, true);
		Mockito.verify(emailService).notifyAccountWillExpire(user1);
	}

	@Test
	public void testCheckExpirationDateSecond() {
		// GIVEN three user with near expiration dates
		User user2 = new User();
		user2.setExpirationDate(LocalDate.now().minusDays(2));

		given(userService.getUsersToReceiveFirstExpirationNotification()).willReturn(Collections.emptyList());
		given(userService.getUsersToReceiveSecondExpirationNotification()).willReturn(Collections.singletonList(user2));
		given(userService.getExpiredUsers()).willReturn(Collections.emptyList());

		// WHEN we check expiration dates
		scheduledTasks.checkExpirationDate();
		
		// THEN second expiration warning is sent to user2
		Mockito.verify(userService).updateExpirationNotification(user2, false);
		Mockito.verify(emailService).notifyAccountWillExpire(user2);
		
	}
}
