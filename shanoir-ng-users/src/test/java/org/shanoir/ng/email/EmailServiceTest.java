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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.shared.email.EmailDatasetsImported;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * User detail service test.
 *
 * @author msimon
 *
 */
@SpringBootTest(properties = "shanoir.instance.name=" + EmailServiceTest.INSTANCE_NAME)
@ActiveProfiles("test")
public class EmailServiceTest {

    /** The name this instance is given for the test, expected in every subject. */
    static final String INSTANCE_NAME = "DEV";

    private static final String SUBJECT_PREFIX = "[" + INSTANCE_NAME + "] ";

    private static final String NEW_PASSWORD = "testPwd";

    @Autowired
    private EmailService emailService;

    private GreenMail greenMail;

    @MockBean
    private UserRepository userRepositoryMock;

    @BeforeEach
    void setup() {
        ServerSetup setup = new ServerSetup(3025, "localhost", "smtp");
        greenMail = new GreenMail(setup);
        greenMail.start();
        given(userRepositoryMock.findAdminEmails()).willReturn(Arrays.asList(new String[]{"admin@test.shanoir.fr"}));
    }

    @AfterEach
    void stopMailServer() {
        greenMail.stop();
    }

    @Test
    public void notifyAccountWillExpireTest() throws Exception {
        emailService.notifyAccountWillExpire(ModelsUtil.createUser());
        assertReceivedMessageContains(SUBJECT_PREFIX + "Account Expiration", "will expire on");
    }

    @Test
    public void notifyNewUserTest() throws Exception {
        emailService.notifyCreateUser(ModelsUtil.createUser(), "password");
        assertReceivedMessageContains(SUBJECT_PREFIX + "Account Creation", "Your account has been created");
    }

    @Test
    @WithMockKeycloakUser(id = 4, username = "phdauvergne", authorities = { "ROLE_ADMIN" })
    public void notifyAccountRequestAcceptedTest() throws Exception {
        emailService.notifyAccountRequestAccepted(ModelsUtil.createUser());
        assertReceivedMessageContains(SUBJECT_PREFIX + "Granted: Your account has been activated", "Your account request has been granted");
    }

    @Test
    public void notifyAccountRequestDeniedTest() throws Exception {
        emailService.notifyAccountRequestDenied(ModelsUtil.createUser());
        assertReceivedMessageContains(SUBJECT_PREFIX + "DENIED: Your account request has been denied", "has been denied");
    }

    @Test
    @WithMockKeycloakUser(id = 4, username = "phdauvergne", authorities = { "ROLE_ADMIN" })
    public void notifyExtensionRequestAcceptedTest() throws Exception {
        emailService.notifyExtensionRequestAccepted(ModelsUtil.createUser());
        assertReceivedMessageContains(SUBJECT_PREFIX + "Granted: Your account extension has been extended", "Your account extension request has been granted");
    }

    @Test
    public void notifyExtensionRequestDeniedTest() throws Exception {
        emailService.notifyExtensionRequestDenied(ModelsUtil.createUser());
        assertReceivedMessageContains(SUBJECT_PREFIX + "DENIED: Your account extension request has been denied", "has been denied");
    }

    @Test
    public void notifyUserResetPasswordTest() throws Exception {
        emailService.notifyUserResetPassword(ModelsUtil.createUser(), NEW_PASSWORD);
        assertReceivedMessageContains(SUBJECT_PREFIX + "Réinitialisation du mot de passe", NEW_PASSWORD);
    }

    @Test
    public void testNotifyStudyManagerDataImported() throws IOException, MessagingException {
        // GIVEN a list of administrators to contact
        User user = new User();
        user.setUsername("username");
        user.setEmail("email@email.com");
        Mockito.when(userRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        Mockito.when(userRepositoryMock.findAllById(Mockito.any(Iterable.class))).thenReturn(Collections.singletonList(user));
        // send back a list of administrators
        EmailDatasetsImported mail = new EmailDatasetsImported();
        mail.setStudyName("StudyName");
        mail.setStudyId("12");
        mail.setUserId(1L);
        mail.setRecipients(Arrays.asList(1L));
        Map<Long, String> datasets = new HashMap<>();
        datasets.put(1L, "test");
        mail.setDatasets(datasets);
        // WHEN we receive an event with elements stating that data was imported successfully
        emailService.notifyStudyManagerDataImported(mail);
        // THEN an email is sent to the administrators
        assertReceivedMessageContains(SUBJECT_PREFIX + "Data imported to StudyName", "imported data to study");
    }

    @Test
    public void sendMassEmailTest() throws IOException, MessagingException {
        final User first = massEmailUser("first@test.shanoir.fr");
        final User second = massEmailUser("second@test.shanoir.fr");

        emailService.sendMassEmail(Arrays.asList(first, second), "[Shanoir] Maintenance",
                "Service unavailable <tomorrow>.\nSorry for the inconvenience.", null, null);

        final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(2, receivedMessages.length);
        assertTrue(receivedMessages[0].getSubject().contains("[Shanoir] Maintenance"));
        final String content = (String) receivedMessages[0].getContent();
        assertTrue(content.contains("Dear"));
        // markup of the announcement is escaped, line breaks are rendered
        assertTrue(content.contains("Service unavailable &lt;tomorrow&gt;.<br/>Sorry for the inconvenience."));
        // a platform wide announcement stays signed by the administrator
        assertTrue(content.contains("Shanoir administrator"));
    }

    @Test
    public void sendStudyMassEmailNamesSenderAndStudyTest() throws IOException, MessagingException {
        final User member = massEmailUser("member@test.shanoir.fr");
        final User sender = massEmailUser("sender@test.shanoir.fr");
        sender.setFirstName("Jane");
        sender.setLastName("Doe");

        emailService.sendMassEmail(Arrays.asList(member), "Kick-off meeting", "See you on monday.", sender,
                "My Study");

        final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        // the study name tells the members what the email relates to
        assertEquals("[My Study] Kick-off meeting", receivedMessages[0].getSubject());
        assertEquals("sender@test.shanoir.fr", ((InternetAddress) receivedMessages[0].getReplyTo()[0]).getAddress());
        final String content = (String) receivedMessages[0].getContent();
        // the members know who addressed them
        assertTrue(content.contains("Jane Doe"));
        assertTrue(content.contains("administrator of the study"));
        assertTrue(content.contains("My Study"));
        assertFalse(content.contains("Shanoir administrator"));
    }

    @Test
    public void sendMassEmailSkipsFailingRecipientTest() {
        final User failing = massEmailUser(null);
        final User valid = massEmailUser("valid@test.shanoir.fr");

        emailService.sendMassEmail(Arrays.asList(failing, valid), "[Shanoir] Maintenance", "Service unavailable.",
                null, null);

        assertEquals(1, greenMail.getReceivedMessages().length);
    }

    private User massEmailUser(final String email) {
        final User user = ModelsUtil.createUser();
        user.setEmail(email);
        return user;
    }

    private void assertReceivedMessageContains(final String expectedSubject, final String expectedContent)
            throws IOException, MessagingException {
        final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertTrue(receivedMessages.length > 0);
        final String subject = receivedMessages[0].getSubject();
        assertTrue(subject.contains(expectedSubject));
        final String content = (String) receivedMessages[0].getContent();
        assertTrue(content.contains(expectedContent));
    }

}
