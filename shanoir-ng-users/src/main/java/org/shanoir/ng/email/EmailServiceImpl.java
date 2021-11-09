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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.shanoir.ng.email.model.DatasetDetail;
import org.shanoir.ng.shared.email.EmailDatasetsImported;
import org.shanoir.ng.shared.email.EmailStudyUsersAdded;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of email service.
 * 
 * @author msimon, mkain
 *
 */
@Service
public class EmailServiceImpl implements EmailService {

	private static final String STUDY_USERS = "studyUsers";

	private static final String EMAIL = "email";

	private static final String EXPIRATION_DATE = "expirationDate";

	private static final String LASTNAME = "lastname";

	private static final String FIRSTNAME = "firstname";
	
	private static final String USERNAME = "username";

	private static final String SERVER_ADDRESS = "serverAddress";
	
	private static final String STUDY_NAME = "studyName";
	
	private static final String SUBJECT = "subject";
	
	private static final String EXAMINATION = "examination";
	
	private static final String EXAM_DATE = "exam_date";

	private static final String STUDY_CARD = "study_card";

	private static final String SERIES = "series";
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailServiceImpl.class);
	
	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private UserRepository userRepository;

	@Value("${server.administrator.email}")
	private String administratorEmail;

	@Value("${front.server.address}")
	private String shanoirServerAddress;

	@Autowired
	RabbitTemplate rabbitTemplate;

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy");

	@Override
	public void notifyAccountWillExpire(User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Shanoir Account Expiration");
			final Map<String, Object> variables = new HashMap<>();
			variables.put(FIRSTNAME, user.getFirstName());
			variables.put(LASTNAME, user.getLastName());
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			variables.put(EXPIRATION_DATE, formatter.format(user.getExpirationDate()));
			final String content = build("notifyAccountWillExpire", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);

	}

	@Override
	public void notifyAdminAccountRequest(final User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("New user account request from " + shanoirServerAddress);
			final Map<String, Object> variables = new HashMap<>();
			variables.put("user", user);
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			final String content = build("notifyAdminAccountRequest", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	@Override
	public void notifyAdminAccountExtensionRequest(User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("User account extension request from " + shanoirServerAddress);
			final Map<String, Object> variables = new HashMap<>();
			variables.put("user", user);
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			final String content = build("notifyAdminAccountExtensionRequest", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	@Override
	public void notifyAccountRequestAccepted(final User user) {
		notifyUserAccountRequestAccepted(user);
		notifyAdminAccountRequestAccepted(user);
	}

	@Override
	public void notifyAccountRequestDenied(final User user) {
		notifyUserAccountRequestDenied(user);
		notifyAdminAccountRequestDenied(user);
	}

	@Override
	public void notifyExtensionRequestAccepted(final User user) {
		notifyUserExtensionRequestAccepted(user);
		notifyAdminExtensionRequestAccepted(user);
	}

	@Override
	public void notifyExtensionRequestDenied(final User user) {
		notifyUserExtensionRequestDenied(user);
		notifyAdminExtensionRequestDenied(user);
	}

	@Override
	public void notifyCreateUser(final User user, final String password) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Shanoir Account Creation");
			final Map<String, Object> variables = new HashMap<>();
			variables.put(FIRSTNAME, user.getFirstName());
			variables.put(LASTNAME, user.getLastName());
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			variables.put("password", password);
			variables.put("username", user.getUsername());
			final String content = build("notifyCreateUser", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);

	}

	@Override
	public void notifyCreateAccountRequest(final User user, final String password) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Shanoir Account Creation");
			final Map<String, Object> variables = new HashMap<>();
			variables.put(FIRSTNAME, user.getFirstName());
			variables.put(LASTNAME, user.getLastName());
			variables.put("password", password);
			variables.put("username", user.getUsername());
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			final String content = build("notifyCreateAccountRequest", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);

	}

	@Override
	public void notifyUserResetPassword(final User user, final String password) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("[Shanoir] Réinitialisation du mot de passe");
			final Map<String, Object> variables = new HashMap<>();
			variables.put(FIRSTNAME, user.getFirstName());
			variables.put(LASTNAME, user.getLastName());
			variables.put("newPassword", password);
			final String content = build("notifyUserResetPassword", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	private String build(final String templateFile, final Map<String, Object> variables) {
		final Context context = new Context();
		if (variables != null) {
			for (final Entry<String, Object> entry : variables.entrySet()) {
				context.setVariable(entry.getKey(), entry.getValue());
			}
		}
		return templateEngine.process(templateFile, context);
	}

	private void notifyAdminAccountRequestAccepted(final User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("User account request granted (" + shanoirServerAddress + ")");
			final Map<String, Object> variables = new HashMap<>();
			variables.put("user", user);
			final String content = build("notifyAdminAccountRequestAccepted", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	private void notifyAdminAccountRequestDenied(final User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("User account request DENIED (" + shanoirServerAddress + ")");
			final Map<String, Object> variables = new HashMap<>();
			variables.put("user", user);
			final String content = build("notifyAdminAccountRequestDenied", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	private void notifyAdminExtensionRequestAccepted(final User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("User account request granted (" + shanoirServerAddress + ")");
			final Map<String, Object> variables = new HashMap<>();
			variables.put("user", user);
			final String content = build("notifyAdminExtensionRequestAccepted", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	private void notifyAdminExtensionRequestDenied(final User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("User account request DENIED (" + shanoirServerAddress + ")");
			final Map<String, Object> variables = new HashMap<>();
			variables.put("user", user);
			final String content = build("notifyAdminExtensionRequestDenied", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	private void notifyUserAccountRequestAccepted(final User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Granted: Your Shanoir account has been activated");
			final Map<String, Object> variables = new HashMap<>();
			variables.put(FIRSTNAME, user.getFirstName());
			variables.put(LASTNAME, user.getLastName());
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			final String content = build("notifyUserAccountRequestAccepted", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	private void notifyUserAccountRequestDenied(final User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("DENIED: Your Shanoir account request has been denied");
			final Map<String, Object> variables = new HashMap<>();
			variables.put("administratorEmail", administratorEmail);
			variables.put(FIRSTNAME, user.getFirstName());
			variables.put(LASTNAME, user.getLastName());
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			final String content = build("notifyUserAccountRequestDenied", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	private void notifyUserExtensionRequestAccepted(final User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Granted: Your Shanoir account extension has been extended");
			final Map<String, Object> variables = new HashMap<>();
			variables.put(FIRSTNAME, user.getFirstName());
			variables.put(LASTNAME, user.getLastName());
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			variables.put(EXPIRATION_DATE, formatter.format(user.getExpirationDate()));
			final String content = build("notifyUserExtensionRequestAccepted", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	private void notifyUserExtensionRequestDenied(final User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("DENIED: Your Shanoir account extension request has been denied");
			final Map<String, Object> variables = new HashMap<>();
			variables.put(FIRSTNAME, user.getFirstName());
			variables.put(LASTNAME, user.getLastName());
			variables.put(SERVER_ADDRESS, shanoirServerAddress);
			variables.put(EXPIRATION_DATE, formatter.format(user.getExpirationDate()));
			final String content = build("notifyUserExtensionRequestDenied", variables);
			messageHelper.setText(content, true);
		};
		mailSender.send(messagePreparator);
	}

	@Override
	public void notifyStudyManagerDataImported(EmailDatasetsImported generatedMail) {
        // Find user that imported
        User u = userRepository.findById(generatedMail.getUserId()).orElse(null);

		// Get the list of recipients
		List<User> admins = (List<User>) this.userRepository.findAllById(generatedMail.getRecipients());
		
		List<DatasetDetail> datasetLinks = new ArrayList<>();
		for (Entry<Long, String> dataset :  generatedMail.getDatasets().entrySet()) {
			DatasetDetail detail = new DatasetDetail();
			detail.setName(dataset.getValue());
			detail.setUrl(this.shanoirServerAddress + "dataset/details/" + dataset.getKey());
			datasetLinks.add(detail);
		}
		
		DatasetDetail examDetail = new DatasetDetail();
		examDetail.setName(generatedMail.getExaminationId());
		examDetail.setUrl(shanoirServerAddress + "examination/details/" + generatedMail.getExaminationId());

		for (User admin : admins) {
			MimeMessagePreparator messagePreparator = mimeMessage -> {
				final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
				messageHelper.setFrom(administratorEmail);
				messageHelper.setTo(admin.getEmail());
				messageHelper.setSubject("[Shanoir] Data imported to " + generatedMail.getStudyName());
				final Map<String, Object> variables = new HashMap<>();
				variables.put(LASTNAME, admin.getLastName());
				variables.put(FIRSTNAME, admin.getFirstName());
				variables.put(USERNAME, u.getUsername());
				variables.put(STUDY_NAME, generatedMail.getStudyName());
				variables.put(SUBJECT, generatedMail.getSubjectName());
				variables.put(SERIES, datasetLinks);
				variables.put(EXAMINATION, examDetail);
				variables.put(EXAM_DATE, generatedMail.getExamDate());
				variables.put(STUDY_CARD, generatedMail.getStudyCard());
				variables.put(SERVER_ADDRESS, shanoirServerAddress);
				final String content = build("notifyStudyAdminDataImported", variables);
				LOG.info(content);
				messageHelper.setText(content, true);
			};
			// Send the message
			LOG.info("Sending import mail to {} for study {}", admin.getUsername(), generatedMail.getStudyId());
			mailSender.send(messagePreparator);
		}
	}

	@Override
	public void notifyStudyManagerStudyUsersAdded(EmailStudyUsersAdded email) {
        // Find user that edited the study
        User user = userRepository.findById(email.getUserId()).orElse(null);

		// Get the list of recipients
		List<User> studyAdmins = (List<User>) this.userRepository.findAllById(email.getRecipients());
		
		List<User> newStudyUsers = this.userRepository.findByIdIn(email.getStudyUsers());
		
		for (User studyAdmin : studyAdmins) {
			MimeMessagePreparator messagePreparator = mimeMessage -> {
				final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
				messageHelper.setFrom(administratorEmail);
				messageHelper.setCc(user.getEmail());
				messageHelper.setTo(studyAdmin.getEmail());
				messageHelper.setSubject("[Shanoir] Member(s) added to " + email.getStudyName());
				final Map<String, Object> variables = new HashMap<>();
				variables.put(FIRSTNAME, studyAdmin.getFirstName());
				variables.put(LASTNAME, studyAdmin.getLastName());
				variables.put(EMAIL, user.getEmail());
				variables.put(STUDY_NAME, email.getStudyName());
				variables.put(STUDY_USERS, newStudyUsers);
				variables.put(SERVER_ADDRESS, shanoirServerAddress + "study/edit/" + email.getStudyId());
				final String content = build("notifyStudyAdminStudyUsersAdded", variables);
				LOG.info(content);
				messageHelper.setText(content, true);
			};
			// Send the message
			LOG.info("Sending study-users-added mail to {} for study {}", studyAdmin.getUsername(), email.getStudyId());
			mailSender.send(messagePreparator);
		}
	}

}
