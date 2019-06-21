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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.accountrequest.AccountRequestInfo;
import org.shanoir.ng.user.User;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

/**
 * User detail service test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceTest {

	private static final String NEW_PASSWORD = "testPwd";
	
	@Autowired
	private EmailServiceImpl emailService;
	
	private GreenMail smtpServer;

	@Before
	public void setup() {
		smtpServer = new GreenMail(ServerSetupTest.SMTP);
		smtpServer.start();
	}

	@After
	public void tearDown() throws Exception {
		smtpServer.stop();
	}

	@Test
	public void notifyAccountWillExpireTest() throws Exception {
		emailService.notifyAccountWillExpire(ModelsUtil.createUser());

		assertReceivedMessageContains("Shanoir Account Expiration", "will expire on");
	}

	@Test
	public void notifyAdminAccountRequestTest() throws Exception {
		final User user = ModelsUtil.createUser(null);
		final AccountRequestInfo info = new AccountRequestInfo();
		info.setContact("contact");
		info.setFunction("function");
		info.setInstitution("institution");
		info.setService("service");
		info.setStudy("study");
		info.setWork("work");
		user.setAccountRequestInfo(info);
		
		emailService.notifyAdminAccountRequest(user);

		assertReceivedMessageContains("New user account request from", "is requesting an account on");
	}

	@Test
	public void notifyNewUserTest() throws Exception {
		emailService.notifyNewUser(ModelsUtil.createUser(), "password");

		assertReceivedMessageContains("Shanoir Account Creation", "An account has been created");
	}

	@Test
	public void notifyAccountRequestAcceptedTest() throws Exception {
		emailService.notifyAccountRequestAccepted(ModelsUtil.createUser());

		assertReceivedMessageContains("Granted: Your Shanoir account has been activated",
				"Your account request has been granted");
	}

	@Test
	public void notifyAccountRequestDeniedTest() throws Exception {
		emailService.notifyAccountRequestDenied(ModelsUtil.createUser());

		assertReceivedMessageContains("DENIED: Your Shanoir account request has been denied",
				"has been denied");
	}

	@Test
	public void notifyExtensionRequestAcceptedTest() throws Exception {
		emailService.notifyExtensionRequestAccepted(ModelsUtil.createUser());

		assertReceivedMessageContains("Granted: Your Shanoir account extension has been extended",
				"Your account extension request has been granted");
	}

	@Test
	public void notifyExtensionRequestDeniedTest() throws Exception {
		emailService.notifyExtensionRequestDenied(ModelsUtil.createUser());

		assertReceivedMessageContains("DENIED: Your Shanoir account extension request has been denied",
				"has been denied");
	}

	@Test
	public void notifyUserResetPasswordTest() throws Exception {
		emailService.notifyUserResetPassword(ModelsUtil.createUser(), NEW_PASSWORD);

		assertReceivedMessageContains("[Shanoir] Réinitialisation du mot de passe",
				NEW_PASSWORD);
	}

	private void assertReceivedMessageContains(final String expectedSubject, final String expectedContent)
			throws IOException, MessagingException {
		final MimeMessage[] receivedMessages = smtpServer.getReceivedMessages();
		assertTrue(receivedMessages.length > 0);
		final String subject = (String) receivedMessages[0].getSubject();
		assertTrue(subject.contains(expectedSubject));
		final String content = (String) receivedMessages[0].getContent();
		assertTrue(content.contains(expectedContent));
	}

}
