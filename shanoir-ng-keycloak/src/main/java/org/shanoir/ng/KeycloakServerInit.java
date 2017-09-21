package org.shanoir.ng;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.shanoir.ng.user.User;
import org.shanoir.ng.user.UserRepository;
import org.shanoir.ng.utils.KeycloakUtils;
import org.shanoir.ng.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Hello world!
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class KeycloakServerInit extends SpringBootServletInitializer {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(KeycloakServerInit.class);

	private static final String KEYCLOAK_USER_ENV = "KEYCLOAK_USER";
	private static final String KEYCLOAK_PASSWORD_ENV = "KEYCLOAK_PASSWORD";

	@Value("${keycloak.auth-server-url}")
	private String keycloakAuthServerUrl;

	@Value("${keycloak.realm}")
	private String keycloakRealm;

	private String keycloakRequestsAdminLogin;

	private String keycloakRequestsAdminPassword;

	@Value("${kc.requests.client.id}")
	private String keycloakRequestsClientId;

	@Value("${kc.requests.realm}")
	private String keycloakRequestsRealm;

	@Value("${kc.requests.debug.use.dummy.password}")
	private boolean keycloakUseDummyPassword;

	@Autowired
	private UserRepository userRepository;

	private Keycloak keycloak;

	/**
	 * @return the keycloakRequestsAdminLogin
	 */
	protected String getKeycloakRequestsAdminLogin() {
		if (keycloakRequestsAdminLogin == null) {
			keycloakRequestsAdminLogin = System.getenv(KEYCLOAK_USER_ENV);
		}
		return keycloakRequestsAdminLogin;
	}

	/**
	 * @return the keycloakRequestsAdminPassword
	 */
	protected String getKeycloakRequestsAdminPassword() {
		if (keycloakRequestsAdminPassword == null) {
			keycloakRequestsAdminPassword = System.getenv(KEYCLOAK_PASSWORD_ENV);
		}
		return keycloakRequestsAdminPassword;
	}

	protected Keycloak getKeycloak() {
		if (keycloak == null) {
			keycloak = Keycloak.getInstance(keycloakAuthServerUrl, keycloakRequestsRealm,
					getKeycloakRequestsAdminLogin(), getKeycloakRequestsAdminPassword(), keycloakRequestsClientId);
		}
		return keycloak;
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = null;
		try {
			context = SpringApplication.run(KeycloakServerInit.class, args);

			context.getBean(KeycloakServerInit.class).start();
		} catch (Exception e) {
			LOG.error("Error while initializing Keycloak server", e);
			System.exit(1);
		} finally {
			if (context != null) {
				context.close();
			}
		}
	}

	private void start() {
		// Roles already created by shanoir-ng-realm.json
		createUsers();
	}

	private void createUsers() {
		LOG.info("Create users");

		final Iterable<User> users = userRepository.findAll();
		for (final User user : users) {
			final String keycloakId = KeycloakUtils
					.getCreatedUserId(getKeycloak().realm(keycloakRealm).users().create(getUserRepresentation(user)));
			user.setKeycloakId(keycloakId);
			userRepository.save(user);

			final UserResource userResource = getKeycloak().realm(keycloakRealm).users().get(keycloakId);
			// Reset user password
			final CredentialRepresentation credential = new CredentialRepresentation();
			credential.setType(CredentialRepresentation.PASSWORD);
			if (keycloakUseDummyPassword) {
				// debug setup: use a fixed password for everybody
				credential.setValue("&a1A&a1A");
				credential.setTemporary(false);
			} else {
				// normal setup: generate a random password and force the user to change it
				credential.setValue(PasswordUtils.generatePassword());
				credential.setTemporary(true);
			}
			userResource.resetPassword(credential);

			// Add realm role
			userResource.roles().realmLevel().add(Arrays.asList(
					getKeycloak().realm(keycloakRealm).roles().get(user.getRole().getName()).toRepresentation()));
		}
	}

	/*
	 * Parse user to keycloak user representation.
	 * 
	 * @param user user.
	 * 
	 * @return keycloak user representation.
	 */
	private UserRepresentation getUserRepresentation(final User user) {
		final Map<String, List<String>> attributes = new HashMap<String, List<String>>();
		attributes.put("userId", Arrays.asList(user.getId().toString()));
		if (user.getExpirationDate() != null) {
			attributes.put("expirationDate", Arrays.asList("" + user.getExpirationDate().getTime()));
		}

		final UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setAttributes(attributes);
		userRepresentation.setEmail(user.getEmail());
		userRepresentation.setEmailVerified(Boolean.TRUE);
		userRepresentation.setEnabled(user.isEnabled());
		userRepresentation.setFirstName(user.getFirstName());
		userRepresentation.setLastName(user.getLastName());
		userRepresentation.setUsername(user.getUsername());

		return userRepresentation;
	}

}
