package org.shanoir.ng.email;

import static org.junit.Assert.assertEquals;
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
	public void notifyUserAccountRequestAcceptedTest() throws Exception {
		emailService.notifyUserAccountRequestAccepted(ModelsUtil.createUser());

		assertReceivedMessageContains("Granted: Your Shanoir account has been activated",
				"Your account request has been granted");
	}

	@Test
	public void notifyUserExtensionRequestAcceptedTest() throws Exception {
		emailService.notifyUserExtensionRequestAccepted(ModelsUtil.createUser());

		assertReceivedMessageContains("Granted: Your Shanoir account has been extended",
				"Your account extension request has been granted");
	}

	@Test
	public void notifyUserExtensionRequestDeniedTest() throws Exception {
		emailService.notifyUserExtensionRequestDenied(ModelsUtil.createUser());

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
		assertEquals(1, receivedMessages.length);
		final String subject = (String) receivedMessages[0].getSubject();
		assertTrue(subject.contains(expectedSubject));
		final String content = (String) receivedMessages[0].getContent();
		assertTrue(content.contains(expectedContent));
	}

}
