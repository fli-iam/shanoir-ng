package org.shanoir.ng.email;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.shanoir.ng.user.User;
import org.shanoir.ng.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of email service.
 * 
 * @author msimon
 *
 */
@Service
public class EmailServiceImpl implements EmailService {

	/**
	 * Logger
	 */
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

	private static final DateFormat shortDateFormatEN = DateFormat.getDateInstance(DateFormat.SHORT,
			new Locale("EN", "en"));

	@Override
	public void notifyAccountWillExpire(User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Shanoir Account Expiration");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("firstname", user.getFirstName());
			variables.put("lastname", user.getLastName());
			variables.put("serverAddress", shanoirServerAddress);
			variables.put("expirationDate", shortDateFormatEN.format(user.getExpirationDate()));
			final String content = build("notifyAccountWillExpire", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
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
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("user", user);
			variables.put("serverAddress", shanoirServerAddress);
			final String content = build("notifyAdminAccountRequest", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
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
	public void notifyNewUser(final User user, final String password) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Shanoir Account Creation");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("firstname", user.getFirstName());
			variables.put("lastname", user.getLastName());
			variables.put("password", password);
			variables.put("username", user.getUsername());
			final String content = build("notifyNewUser", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	@Override
	public void notifyUserResetPassword(final User user, final String password) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("[Shanoir] Réinitialisation du mot de passe");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("firstname", user.getFirstName());
			variables.put("lastname", user.getLastName());
			variables.put("newPassword", password);
			final String content = build("notifyUserResetPassword", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	private String build(final String templateFile, final Map<String, Object> variables) {
		final Context context = new Context();
		if (variables != null) {
			for (final String variable : variables.keySet()) {
				context.setVariable(variable, variables.get(variable));
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
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("user", user);
			final String content = build("notifyAdminAccountRequestAccepted", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	private void notifyAdminAccountRequestDenied(final User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("User account request DENIED (" + shanoirServerAddress + ")");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("user", user);
			final String content = build("notifyAdminAccountRequestDenied", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	private void notifyAdminExtensionRequestAccepted(final User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("User account request granted (" + shanoirServerAddress + ")");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("user", user);
			final String content = build("notifyAdminExtensionRequestAccepted", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	private void notifyAdminExtensionRequestDenied(final User user) {
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();

		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(adminEmails.toArray(new String[0]));
			messageHelper.setSubject("User account request DENIED (" + shanoirServerAddress + ")");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("user", user);
			final String content = build("notifyAdminExtensionRequestDenied", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	private void notifyUserAccountRequestAccepted(final User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Granted: Your Shanoir account has been activated");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("firstname", user.getFirstName());
			variables.put("lastname", user.getLastName());
			variables.put("serverAddress", shanoirServerAddress);
			final String content = build("notifyUserAccountRequestAccepted", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	private void notifyUserAccountRequestDenied(final User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("DENIED: Your Shanoir account request has been denied");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("administratorEmail", administratorEmail);
			variables.put("firstname", user.getFirstName());
			variables.put("lastname", user.getLastName());
			variables.put("serverAddress", shanoirServerAddress);
			final String content = build("notifyUserAccountRequestDenied", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	private void notifyUserExtensionRequestAccepted(final User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("Granted: Your Shanoir account extension has been extended");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("firstname", user.getFirstName());
			variables.put("lastname", user.getLastName());
			variables.put("serverAddress", shanoirServerAddress);
			variables.put("expirationDate", shortDateFormatEN.format(user.getExpirationDate()));
			final String content = build("notifyUserExtensionRequestAccepted", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

	private void notifyUserExtensionRequestDenied(final User user) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(administratorEmail);
			messageHelper.setTo(user.getEmail());
			messageHelper.setSubject("DENIED: Your Shanoir account extension request has been denied");
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("firstname", user.getFirstName());
			variables.put("lastname", user.getLastName());
			variables.put("serverAddress", shanoirServerAddress);
			variables.put("expirationDate", shortDateFormatEN.format(user.getExpirationDate()));
			final String content = build("notifyUserExtensionRequestDenied", variables);
			messageHelper.setText(content, true);
		};
		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			LOG.error("Error while sending email to new user " + user.getEmail(), e);
		}
	}

}
