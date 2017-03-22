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
import org.shanoir.ng.email.EmailServiceImpl;
import org.shanoir.ng.user.User;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

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

	@Autowired
	private EmailServiceImpl emailService;
	
	private GreenMail smtpServer;

	@Before
	public void setup() {
		smtpServer = new GreenMail(new ServerSetup(25, null, "smtp"));
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
